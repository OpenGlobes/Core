/*
 * Copyright (C) 2020 Hongbao Chen <chenhongbao@outlook.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.openglobes.core.trader;

import com.openglobes.core.GatewayRuntimeException;
import com.openglobes.core.ServiceRuntimeStatus;
import com.openglobes.core.data.*;
import com.openglobes.core.utils.Utils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

/**
 * Implementation of service handler to process responses.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class TraderGatewayHandler implements ITraderGatewayHandler {

    private final TraderContext ctx;

    public TraderGatewayHandler(TraderContext context) {
        this.ctx = context;
    }

    @Override
    public void onError(GatewayRuntimeException exception) {
        publishEvent(GatewayRuntimeException.class, exception);
    }

    @Override
    public void onError(Request request, Response response) {
        if (request.getAction() == ActionType.DELETE) {
            publishRquestError(request, response);
            /*
             * Delete action fails, so order is unchanged.
             */
        } else {
            deleteOrderWhenException(request, response);
        }
    }

    @Override
    public void onResponse(Response response) {
        try (var conn = ctx.getEngine().getDataSource().getConnection()) {
            preprocess(response);
            synchronized (ctx.getEngine()) {
                conn.addResponse(response);
                if (response.getAction() == ActionType.DELETE) {
                    dealDelete(response);
                }
            }
            callOnResponse(response);
        } catch (DataException ex) {
            callOnException(new TraderRuntimeException("Fail saving response to data source.",
                                                       ex));
        } catch (SQLException | ClassNotFoundException | TraderException ex) {
            callOnException(new TraderRuntimeException(ex.getMessage(),
                                                       ex));
        }
    }

    @Override
    public void onStatusChange(ServiceRuntimeStatus status) {
        publishEvent(ServiceRuntimeStatus.class, status);
    }

    @Override
    public void onTrade(Trade trade) {
        try {
            preprocess(trade);
            synchronized (ctx.getEngine()) {
                dealTrade(trade);
            }
            callOnTrade(trade);
        } catch (TraderException ex) {
            callOnException(new TraderRuntimeException(ex.getMessage(),
                                                       ex));
        }
    }

    private void callOnException(TraderRuntimeException e) {
        try {
            onError(new GatewayRuntimeException(0,
                                                e.getMessage(),
                                                e));
        } catch (Throwable ignored) {
        }
    }

    private void callOnResponse(Response response) {
        publishEvent(Response.class, response);
    }

    private void callOnTrade(Trade trade) {
        publishEvent(Trade.class, trade);
    }

    private void checkFrozenInfo(Collection<FrozenBundle> bs) throws InvalidFrozenBundleException {
        for (var v : bs) {
            if (v.getCommission() == null || v.getContract() == null || v.getMargin() == null) {
                throw new InvalidFrozenBundleException("Invalid frozen bundle and its fields.");
            }
        }
    }

    private void closeDelete(Response response,
                             ITraderDataConnection conn) throws DataQueryException,
                                                                DataUpdateException,
                                                                DataRemovalException,
                                                                InvalidFrozenBundleException,
                                                                IllegalContractStatusException {
        Collection<FrozenBundle> bs = getFrozenBundles(response.getOrderId(),
                                                       conn);
        for (var b : bs) {
            var s = b.getContract().getStatus();
            if (s != ContractStatus.CLOSING) {
                continue;
            }
            deleteClose(b.getCommission(),
                        b.getContract(),
                        conn);
        }
    }

    private void closeTrade(Trade trade,
                            ITraderDataConnection conn) throws DataQueryException,
                                                               QuantityOverflowException,
                                                               InstrumentNotFoundException,
                                                               InvalidFrozenBundleException,
                                                               IllegalContractStatusException {
        Collection<FrozenBundle> bs = getFrozenBundles(trade.getOrderId(),
                                                       conn);
        int count = 0;
        var it = bs.iterator();
        while (count < trade.getQuantity() && it.hasNext()) {
            var b = it.next();
            var s = b.getContract().getStatus();
            if (s != ContractStatus.CLOSING) {
                continue;
            }
            dealClose(b.getCommission(),
                      b.getMargin(),
                      b.getContract(),
                      trade,
                      conn);
            ++count;
        }
        if (count < trade.getQuantity()) {
            throw new QuantityOverflowException(trade.getQuantity() + ">" + count);
        }
    }

    private void dealClose(Commission commission,
                           Margin margin,
                           Contract contract,
                           Trade trade,
                           ITraderDataConnection conn) throws InstrumentNotFoundException,
                                                              IllegalContractStatusException {
        requireStatus(commission.getStatus(),
                      FeeStatus.FORZEN);
        requireStatus(margin.getStatus(),
                      FeeStatus.DEALED);
        requireStatus(contract.getStatus(),
                      ContractStatus.CLOSING);
        try {
            /*
             * Update commission.
             */
            commission.setStatus(FeeStatus.DEALED);
            conn.updateCommission(commission);
            /*
             * Update margin.
             */
            margin.setStatus(FeeStatus.REMOVED);
            conn.updateMargin(margin);
            /*
             * Update contract.
             */
            var price = trade.getPrice();
            var instrument = getTodayInstrument(trade.getInstrumentId());
            var amount = ctx.getEngine().getAlgorithm().getAmount(price, instrument);
            contract.setCloseAmount(amount);
            contract.setStatus(ContractStatus.CLOSED);
            contract.setCloseTradingDay(trade.getTradingDay());
            conn.updateContract(contract);
        } catch (DataUpdateException ex) {

        }
    }

    private void dealDelete(Response response) throws RequestNotFoundException,
                                                      InvalidRequestOffsetException,
                                                      DataAccessException,
                                                      InvalidFrozenBundleException,
                                                      InvalidDataSourceException,
                                                      IllegalContractStatusException {
        ITraderDataConnection conn = null;
        try {
            /*
             * Get data source and start transaction.
             */
            conn = getDataSource().getConnection();
            conn.transaction();
            /*
             * Add cancel response.
             */
            conn.addResponse(response);
            var o = conn.getRequestByOrderId(response.getOrderId());
            if (o == null) {
                throw new RequestNotFoundException("Order ID: " + response.getOrderId() + ".");
            }
            var offset = o.getOffset();
            if (offset == null) {
                throw new InvalidRequestOffsetException("Order ID: " + o.getOrderId() + ".");
            }
            if (offset == Offset.OPEN) {
                openDelete(response,
                           conn);
            } else {
                closeDelete(response,
                            conn);
            }
            conn.commit();
        } catch (TraderRuntimeException e) {
            rollbackAndCallHandler(conn,
                                   e);
        } catch (DataException | SQLException | ClassNotFoundException ex) {
            throw new DataAccessException(ex.getMessage(),
                                          ex);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void dealOpen(Commission commission,
                          Margin margin,
                          Contract contract,
                          Trade trade,
                          ITraderDataConnection conn) throws InstrumentNotFoundException,
                                                             DataAccessException,
                                                             IllegalContractStatusException {
        requireStatus(commission.getStatus(),
                      FeeStatus.FORZEN);
        requireStatus(margin.getStatus(),
                      FeeStatus.FORZEN);
        requireStatus(contract.getStatus(),
                      ContractStatus.OPENING);
        try {
            /*
             * Update commission.
             */
            commission.setStatus(FeeStatus.DEALED);
            conn.updateCommission(commission);
            /*
             * Update margin.
             */
            margin.setStatus(FeeStatus.DEALED);
            conn.updateMargin(margin);
            /*
             * Update contract.
             */
            var price = trade.getPrice();
            var instrument = getTodayInstrument(trade.getInstrumentId());
            var amount = ctx.getEngine().getAlgorithm().getAmount(price, instrument);
            contract.setOpenAmount(amount);
            contract.setStatus(ContractStatus.OPEN);
            contract.setTradeId(trade.getTradeId());
            contract.setOpenTimestamp(trade.getTimestamp());
            contract.setOpenTradingDay(trade.getTradingDay());
            conn.updateCommission(commission);
        } catch (DataUpdateException ex) {
            throw new DataAccessException(ex.getMessage(),
                                          ex);
        }
    }

    private void dealTrade(Trade trade) throws InvalidTradeOffsetException,
                                               QuantityOverflowException,
                                               InstrumentNotFoundException,
                                               DataAccessException,
                                               InvalidFrozenBundleException,
                                               InvalidDataSourceException,
                                               IllegalContractStatusException {
        ITraderDataConnection conn = null;
        try {
            /*
             * Get data source and start transaction.
             */
            conn = getDataSource().getConnection();
            conn.transaction();
            /*
             * Add trade. Please note that volumn in trade could be zero,
             * notifying a status change of the inserted order request.
             */
            conn.addTrade(trade);
            var offset = trade.getOffset();
            if (offset == null) {
                throw new InvalidTradeOffsetException("Order ID: " + trade.getOrderId() + ".");
            }
            if (Offset.OPEN == offset) {
                openTrade(trade,
                          conn);
            } else {
                closeTrade(trade,
                           conn);
            }
            conn.commit();
        } catch (GatewayRuntimeException e) {
            rollbackAndCallHandler(conn,
                                   e.getCode(),
                                   e.getMessage(),
                                   e);
        } catch (SQLException | DataInsertionException | DataQueryException | ClassNotFoundException ex) {
            throw new DataAccessException(ex.getMessage(),
                                          ex);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void deleteClose(Commission commission,
                             Contract contract,
                             ITraderDataConnection conn) throws DataUpdateException,
                                                                DataRemovalException,
                                                                DataQueryException,
                                                                IllegalContractStatusException {
        requireStatus(contract.getStatus(),
                      ContractStatus.CLOSING);
        contract.setStatus(ContractStatus.OPEN);
        conn.updateContract(contract);
        conn.removeCommission(commission.getCommissionId());
    }

    private void deleteOpen(Commission commission,
                            Margin margin,
                            Contract contract,
                            ITraderDataConnection conn) throws DataRemovalException,
                                                               IllegalContractStatusException {
        requireStatus(commission.getStatus(),
                      FeeStatus.FORZEN);
        requireStatus(margin.getStatus(),
                      FeeStatus.FORZEN);
        requireStatus(contract.getStatus(),
                      ContractStatus.OPENING);
        conn.removeContract(contract.getContractId());
        conn.removeCommission(commission.getCommissionId());
        conn.removeMargin(margin.getMarginId());

    }

    private void deleteOrderWhenException(Request request, Response response) {
        try {
            /*
             * Call cancel handler to cancel a bad request.
             */
            var delete = initResponse(request);
            delete.setAction(ActionType.DELETE);
            delete.setStatusCode(response.getStatusCode());
            delete.setStatusMessage(response.getStatusMessage());
            onResponse(delete);
            /*
             * Call user handler.
             */
            publishRquestError(request, response);
        } catch (Throwable th) {
            callOnException(new TraderRuntimeException(th.getMessage(),
                                                       th));
        }
    }

    private ITraderDataSource getDataSource() throws InvalidDataSourceException {
        var ds = ctx.getEngine().getDataSource();
        if (ds == null) {
            throw new InvalidDataSourceException("Data source null ptr.");
        }
        return ds;
    }

    private Collection<FrozenBundle> getFrozenBundles(Long orderId,
                                                      ITraderDataConnection conn) throws DataQueryException,
                                                                                         InvalidFrozenBundleException {
        final var map = new HashMap<Long, FrozenBundle>(128);
        var ms = conn.getMarginsByOrderId(orderId);
        Objects.requireNonNull(ms);
        var cs = conn.getCommissionsByOrderId(orderId);
        Objects.requireNonNull(cs);
        for (var c : cs) {
            var cid = c.getContractId();
            Objects.requireNonNull(cid);
            var cc = conn.getContractById(cid);
            Objects.requireNonNull(cc);
            var m = getMarginByContractId(cid, ms);
            Objects.requireNonNull(m);
            map.put(cid, new FrozenBundle(c, m, cc));
        }
        checkFrozenInfo(map.values());
        return map.values();
    }

    private Instrument getTodayInstrument(String instrumentId) throws InstrumentNotFoundException {
        var instrument = ctx.getEngine().getTodayInstrument(instrumentId);
        if (instrument == null) {
            throw new InstrumentNotFoundException(instrumentId);
        }
        return instrument;
    }

    private Margin getMarginByContractId(Long contractId, Collection<Margin> ms) {
        for (var m : ms) {
            if (Objects.equals(m.getContractId(), contractId)) {
                return m;
            }
        }
        return null;
    }

    private Long getSrcId(Long destId) throws SourceIdNotFoundException {
        Objects.requireNonNull(destId, "Destinated ID null.");
        var srcId = ctx.getSourceId(destId);
        if (srcId == null) {
            throw new SourceIdNotFoundException("Source ID not found(Destinated ID:" + destId + ").");
        }
        return srcId;
    }

    private Response initResponse(Request request) throws TraderException {
        var r = new Response();
        r.setInstrumentId(request.getInstrumentId());
        r.setOrderId(request.getOrderId());
        r.setTraderId(request.getTraderId());
        r.setTradingDay(ctx.getGatewayInfo().getTradingDay());
        r.setSignature(Utils.nextUuid().toString());
        return r;
    }

    private void openDelete(Response response, ITraderDataConnection conn) throws DataQueryException,
                                                                                  DataRemovalException,
                                                                                  InvalidFrozenBundleException,
                                                                                  IllegalContractStatusException {
        Collection<FrozenBundle> bs = getFrozenBundles(response.getOrderId(),
                                                       conn);
        for (var b : bs) {
            var s = b.getContract().getStatus();
            if (s != ContractStatus.OPENING) {
                continue;
            }
            deleteOpen(b.getCommission(),
                       b.getMargin(),
                       b.getContract(),
                       conn);
        }
    }

    private void openTrade(Trade trade, ITraderDataConnection conn) throws DataQueryException,
                                                                           QuantityOverflowException,
                                                                           InstrumentNotFoundException,
                                                                           DataAccessException,
                                                                           InvalidFrozenBundleException,
                                                                           IllegalContractStatusException {
        /*
         * Deal opening order.
         */
        Collection<FrozenBundle> bs = getFrozenBundles(trade.getOrderId(),
                                                       conn);
        int count = 0;
        var it = bs.iterator();
        while (count < trade.getQuantity()
               && it.hasNext()) {
            var b = it.next();
            var s = b.getContract().getStatus();
            if (s != ContractStatus.OPENING) {
                continue;
            }
            dealOpen(b.getCommission(),
                     b.getMargin(),
                     b.getContract(),
                     trade,
                     conn
            );
            ++count;
        }
        if (count < trade.getQuantity()) {
            throw new QuantityOverflowException(trade.getQuantity() + ">" + count);
        }
    }

    private void preprocess(Trade trade) throws SourceIdNotFoundException {
        /*
         * Order is deleted, so count down to zero.
         */
        ctx.countDown(trade.getOrderId(),
                      trade.getQuantity());
        trade.setOrderId(getSrcId(trade.getOrderId()));
        trade.setTraderId(ctx.getTraderId());
    }

    private void preprocess(Response response) throws SourceIdNotFoundException {

        var rest = ctx.getDownCountByDestId(response.getOrderId());
        Objects.requireNonNull(rest,
                               "Count down not found(Order ID: " + response.getOrderId() + ").");
        /*
         * Order is deleted, so count down to zero.
         */
        ctx.countDown(response.getOrderId(),
                      rest);
        response.setOrderId(getSrcId(response.getOrderId()));
        response.setTraderId(ctx.getTraderId());
    }

    private <T> void publishEvent(Class<T> clazz, T object) {
        var e = (TraderEngine) ctx.getEngine();
        e.publishEvent(clazz, object);
    }

    private void publishRquestError(Request request, Response response) {
        var r = new EngineRequestError();
        r.setRequest(request);
        r.setResponse(response);
        publishEvent(EngineRequestError.class, r);
    }

    private void requireStatus(Integer saw, Integer wanted) throws IllegalContractStatusException {
        if (!Objects.equals(saw, wanted)) {
            throw new IllegalContractStatusException("Expect " + wanted + " but " + saw + ".");
        }
    }

    private void rollback(ITraderDataConnection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ex) {
            callOnException(new TraderRuntimeException(ex.getMessage(), ex));
        }
    }

    private void rollbackAndCallHandler(ITraderDataConnection conn,
                                        Integer code,
                                        String message,
                                        Throwable th) {
        rollback(conn);
        callOnException(new TraderRuntimeException(message + "(" + code + ")",
                                                   th));
    }

    private void rollbackAndCallHandler(ITraderDataConnection conn,
                                        TraderRuntimeException e) {
        rollback(conn);
        callOnException(e);
    }

    private class FrozenBundle {

        private final Contract contract;
        private final Margin margin;
        private Commission commission;

        FrozenBundle(Commission commission, Margin margin, Contract contract) {
            this.commission = commission;
            this.margin = margin;
            this.contract = contract;
        }

        Commission getCommission() {
            return commission;
        }

        void setCommission(Commission commission) {
            this.commission = commission;
        }

        Contract getContract() {
            return contract;
        }

        Margin getMargin() {
            return margin;
        }

    }
}
