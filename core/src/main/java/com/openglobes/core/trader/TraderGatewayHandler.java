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

import com.openglobes.core.data.DataSourceException;
import com.openglobes.core.data.ITraderData;
import com.openglobes.core.data.ITraderDataSource;
import com.openglobes.core.exceptions.EngineException;
import com.openglobes.core.exceptions.EngineRuntimeException;
import com.openglobes.core.exceptions.GatewayException;
import com.openglobes.core.exceptions.GatewayRuntimeException;
import com.openglobes.core.exceptions.ServiceRuntimeStatus;
import com.openglobes.core.utils.Utils;
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
    public void onException(GatewayRuntimeException exception) {
        publishEvent(GatewayRuntimeException.class, exception);
    }

    @Override
    public void onException(Request request,
                            GatewayRuntimeException exception,
                            int requestId) {
        if (request.getAction() == ActionType.DELETE) {
            publishRquestError(request,
                               exception,
                               requestId);
            /*
             * Delete action fails, so order is unchanged.
             */
        }
        else {
            deleteOrderWhenException(request,
                                     exception,
                                     requestId);
        }
    }

    @Override
    public void onResponse(Response response) {
        try {
            ctx.getEngine().getDataSource().getConnection().addResponse(response);
            preprocess(response);
            if (response.getAction() == ActionType.DELETE) {
                dealDelete(response);
            }
            callOnResponse(response);
        }
        catch (DataSourceException ex) {
            callOnException(new EngineRuntimeException(Exceptions.DS_FAILURE_UNFIXABLE.code(),
                                                       "Fail saving response to data source.",
                                                       ex));
        }
        catch (EngineException ex) {
            callOnException(new EngineRuntimeException(Exceptions.PREPROCESS_RESPONSE_FAIL.code(),
                                                       Exceptions.PREPROCESS_RESPONSE_FAIL.message(),
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
            dealTrade(trade);
            callOnTrade(trade);
        }
        catch (EngineException ex) {
            callOnException(new EngineRuntimeException(Exceptions.PREPROCESS_TRADE_FAIL.code(),
                                                       Exceptions.PREPROCESS_TRADE_FAIL.message(),
                                                       ex));
        }
    }

    private void callOnException(EngineRuntimeException e) {
        try {
            onException(new GatewayRuntimeException(e.getCode(),
                                                    e.getMessage(),
                                                    e));
        }
        catch (Throwable ignored) {
        }
    }

    private void callOnResponse(Response response) {
        publishEvent(Response.class, response);
    }

    private void callOnTrade(Trade trade) {
        publishEvent(Trade.class, trade);
    }

    private void checkCommissionsNull(Collection<Commission> cs) {
        if (cs == null) {
            throw new GatewayRuntimeException(Exceptions.COMMISSION_NULL.code(),
                                              Exceptions.COMMISSION_NULL.message());
        }
    }

    private void checkContractIdNull(Long cid) {
        if (cid == null) {
            throw new GatewayRuntimeException(Exceptions.CONTRACT_ID_NULL.code(),
                                              Exceptions.CONTRACT_ID_NULL.message());
        }
    }

    private void checkContractNull(Contract cc) {
        if (cc == null) {
            throw new GatewayRuntimeException(Exceptions.CONTRACT_NULL.code(),
                                              Exceptions.CONTRACT_NULL.message());
        }
    }

    private void checkFrozenInfo(Collection<FrozenBundle> bs) {
        bs.forEach(v -> {
            if (v.getCommission() == null || v.getContract() == null || v.getMargin() == null) {
                throw new GatewayRuntimeException(Exceptions.INCONSISTENT_FROZEN_INFO.code(),
                                                  Exceptions.INCONSISTENT_FROZEN_INFO.message());
            }
        });
    }

    private void checkMarginNull(Margin n) {
        if (n == null) {
            throw new GatewayRuntimeException(Exceptions.MARGIN_NULL.code(),
                                              Exceptions.MARGIN_NULL.message());
        }
    }

    private void checkMarginsNull(Collection<Margin> cs) {
        if (cs == null) {
            throw new GatewayRuntimeException(Exceptions.MARGIN_NULL.code(),
                                              Exceptions.MARGIN_NULL.message());
        }
    }

    private void closeDelete(Response response, ITraderData conn) throws DataSourceException {
        var bs = getFrozenBundles(response.getOrderId(), conn);
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

    private void closeTrade(Trade trade, ITraderData conn) throws EngineException {
        var bs = getFrozenBundles(trade.getOrderId(), conn);
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
            throw new EngineRuntimeException(Exceptions.INCONSISTENT_FROZEN_INFO.code(),
                                             Exceptions.INCONSISTENT_FROZEN_INFO.message());
        }
    }

    private void dealClose(Commission commission,
                           Margin margin,
                           Contract contract,
                           Trade response,
                           ITraderData conn) throws EngineException {
        requireStatus(commission, FeeStatus.FORZEN);
        requireStatus(margin, FeeStatus.DEALED);
        requireStatus(contract, ContractStatus.CLOSING);
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
        var price = response.getPrice();
        var instrument = getInstrument(response.getInstrumentId());
        var amount = ctx.getEngine().getAlgorithm().getAmount(price, instrument);
        contract.setCloseAmount(amount);
        contract.setStatus(ContractStatus.CLOSED);
        conn.updateContract(contract);
    }

    private void dealDelete(Response response) throws DataSourceException {
        ITraderData conn = null;
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
                throw new EngineRuntimeException(Exceptions.ORDER_ID_NOT_FOUND.code(),
                                                 Exceptions.ORDER_ID_NOT_FOUND.message());
            }
            var offset = o.getOffset();
            if (offset == null) {
                throw new EngineRuntimeException(Exceptions.OFFSET_NULL.code(),
                                                 Exceptions.OFFSET_NULL.message());
            }
            if (offset == Offset.OPEN) {
                openDelete(response, conn);
            }
            else {
                closeDelete(response, conn);
            }
            conn.commit();
        }
        catch (GatewayException e) {
            if (conn != null) {
                rollback(conn);
            }
            callOnException(new EngineRuntimeException(e.getCode(),
                                                       e.getMessage(),
                                                       e));
        }
        catch (EngineRuntimeException e) {
            if (conn != null) {
                rollback(conn);
            }
            callOnException(e);
        }
    }

    private void dealOpen(Commission commission,
                          Margin margin,
                          Contract contract,
                          Trade trade,
                          ITraderData conn) throws EngineException {
        requireStatus(commission, FeeStatus.FORZEN);
        requireStatus(margin, FeeStatus.FORZEN);
        requireStatus(contract, ContractStatus.OPENING);
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
        var instrument = getInstrument(trade.getInstrumentId());
        var amount = ctx.getEngine().getAlgorithm().getAmount(price, instrument);
        contract.setOpenAmount(amount);
        contract.setStatus(ContractStatus.OPEN);
        contract.setTradeId(trade.getTradeId());
        contract.setOpenTimestamp(trade.getTimestamp());
        contract.setOpenTradingDay(trade.getTradingDay());
        conn.updateCommission(commission);
    }

    private void dealTrade(Trade trade) throws EngineException {
        ITraderData conn = null;
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
                throw new GatewayRuntimeException(Exceptions.OFFSET_NULL.code(),
                                                  Exceptions.OFFSET_NULL.message());
            }
            if (Offset.OPEN == offset) {
                openTrade(trade, conn);
            }
            else {
                closeTrade(trade, conn);
            }
            conn.commit();
        }
        catch (GatewayException e) {
            if (conn != null) {
                rollback(conn);
            }
            callOnException(new EngineRuntimeException(e.getCode(),
                                                       e.getMessage(),
                                                       e));
        }
        catch (GatewayRuntimeException e) {
            if (conn != null) {
                rollback(conn);
            }
            callOnException(new EngineRuntimeException(e.getCode(),
                                                       e.getMessage(),
                                                       e));
        }
    }

    private void deleteClose(Commission commission,
                             Contract contract,
                             ITraderData conn) throws DataSourceException {
        requireStatus(contract, ContractStatus.CLOSING);
        contract.setStatus(ContractStatus.OPEN);
        conn.updateContract(contract);
        conn.removeCommission(commission.getCommissionId());
    }

    private void deleteOpen(Commission commission,
                            Margin margin,
                            Contract contract,
                            ITraderData conn) throws DataSourceException {
        requireStatus(commission, FeeStatus.FORZEN);
        requireStatus(margin, FeeStatus.FORZEN);
        requireStatus(contract, ContractStatus.OPENING);
        conn.removeContract(contract.getContractId());
        conn.removeCommission(commission.getCommissionId());
        conn.removeMargin(margin.getMarginId());

    }

    private void deleteOrderWhenException(Request request,
                                          GatewayRuntimeException exception,
                                          int requestId) {
        try {
            /*
             * Call cancel handler to cancel a bad request.
             */
            var delete = initResponse(request);
            delete.setAction(ActionType.DELETE);
            delete.setStatusCode(exception.getCode());
            delete.setStatusMessage(exception.getMessage());
            onResponse(delete);
            /*
             * Call user handler.
             */
            publishRquestError(request,
                               exception,
                               requestId);
        }
        catch (Throwable th) {
            callOnException(new EngineRuntimeException(Exceptions.USER_CODE_ERROR.code(),
                                                       Exceptions.USER_CODE_ERROR.message(),
                                                       th));
        }
    }

    private ITraderDataSource getDataSource() throws GatewayException {
        var ds = ctx.getEngine().getDataSource();
        if (ds == null) {
            throw new GatewayException(Exceptions.DATASOURCE_NULL.code(),
                                       Exceptions.DATASOURCE_NULL.message());
        }
        return ds;
    }

    private Collection<FrozenBundle> getFrozenBundles(Long orderId, ITraderData conn) throws DataSourceException {
        final var map = new HashMap<Long, FrozenBundle>(128);
        var ms = conn.getMarginsByOrderId(orderId);
        checkMarginsNull(ms);
        var cs = conn.getCommissionsByOrderId(orderId);
        checkCommissionsNull(cs);
        for (var c : cs) {
            var cid = c.getContractId();
            checkContractIdNull(cid);
            var cc = conn.getContractById(cid);
            checkContractNull(cc);
            var m = getMarginByContractId(cid, ms);
            checkMarginNull(m);
            map.put(cid, new FrozenBundle(c, m, cc));
        }
        checkFrozenInfo(map.values());
        return map.values();
    }

    private Instrument getInstrument(String instrumentId) throws EngineException {
        var instrument = ctx.getEngine().getRelatedInstrument(instrumentId);
        if (instrument == null) {
            throw new EngineException(
                    Exceptions.INSTRUMENT_NULL.code(),
                    Exceptions.INSTRUMENT_NULL.message() + "(Instrument ID:"
                    + instrumentId + ")");
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

    private Long getSrcId(Long destId) {
        if (destId == null) {
            throw new NullPointerException("Destinated ID null.");
        }
        var srcId = ctx.getSourceId(destId);
        if (srcId == null) {
            throw new NullPointerException("Source ID not found(Destinated ID:" + destId + ").");
        }
        return srcId;
    }

    private Response initResponse(Request request) throws EngineException {
        var r = new Response();
        r.setInstrumentId(request.getInstrumentId());
        r.setOrderId(request.getOrderId());
        r.setTraderId(request.getTraderId());
        r.setTradingDay(ctx.getGatewayInfo().getTradingDay());
        r.setSignature(Utils.nextUuid().toString());
        return r;
    }

    private void openDelete(Response response, ITraderData conn) throws DataSourceException {
        var bs = getFrozenBundles(response.getOrderId(), conn);
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

    private void openTrade(Trade trade, ITraderData conn) throws EngineException {
        /*
         * Deal opening order.
         */
        var bs = getFrozenBundles(trade.getOrderId(), conn);
        int count = 0;
        var it = bs.iterator();
        while (count < trade.getQuantity() && it.hasNext()) {
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
            throw new EngineRuntimeException(Exceptions.INCONSISTENT_FROZEN_INFO.code(),
                                             Exceptions.INCONSISTENT_FROZEN_INFO.message());
        }
    }

    private void preprocess(Trade trade) throws EngineException {
        try {
            /*
             * Order is deleted, so count down to zero.
             */
            ctx.countDown(trade.getOrderId(), trade.getQuantity());
            trade.setOrderId(getSrcId(trade.getOrderId()));
            trade.setTraderId(ctx.getTraderId());
        }
        catch (Throwable th) {
            throw new EngineException(Exceptions.PREPROC_RSPS_FAILED.code(),
                                      Exceptions.PREPROC_RSPS_FAILED.message(),
                                      th);
        }
    }

    private void preprocess(Response response) throws EngineException {
        try {
            var rest = ctx.getDownCountByDestId(response.getOrderId());
            if (rest == null) {
                throw new NullPointerException("Count down not found(" + response.getOrderId() + ").");
            }
            /*
             * Order is deleted, so count down to zero.
             */
            ctx.countDown(response.getOrderId(), rest);
            response.setOrderId(getSrcId(response.getOrderId()));
            response.setTraderId(ctx.getTraderId());
        }
        catch (Throwable th) {
            throw new EngineException(Exceptions.PREPROC_RSPS_FAILED.code(),
                                      Exceptions.PREPROC_RSPS_FAILED.message(),
                                      th);
        }
    }

    private <T> void publishEvent(Class<T> clazz, T object) {
        var e = (TraderEngine) ctx.getEngine();
        e.publishEvent(clazz, object);
    }

    private void publishRquestError(Request request,
                                    GatewayRuntimeException exception,
                                    int requestId) {
        var r = new EngineRequestError();
        r.setRequest(request);
        r.setRequestId(requestId);
        r.setException(new EngineRuntimeException(exception.getCode(),
                                                  exception.getMessage(),
                                                  exception));
        publishEvent(EngineRequestError.class, r);
    }

    private void requireStatus(Contract c, ContractStatus s) {
        if (!Objects.equals(c.getStatus(), s)) {
            throw new GatewayRuntimeException(Exceptions.INVALID_DELETING_CONTRACT_STATUS.code(),
                                              Exceptions.INVALID_DELETING_CONTRACT_STATUS.message()
                                              + "(Contract ID:" + c.getContractId() + ")");
        }
    }

    private void requireStatus(Margin m, FeeStatus s) {
        if (!Objects.equals(m.getStatus(), s)) {
            throw new GatewayRuntimeException(Exceptions.INVALID_DELETING_MARGIN_STATUS.code(),
                                              Exceptions.INVALID_DELETING_MARGIN_STATUS.message()
                                              + "(Contract ID:" + m.getMarginId() + ")");
        }
    }

    private void requireStatus(Commission c, FeeStatus s) {
        if (!Objects.equals(c.getStatus(), s)) {
            throw new GatewayRuntimeException(Exceptions.INVALID_DELETING_COMMISSION_STATUS.code(),
                                              Exceptions.INVALID_DELETING_COMMISSION_STATUS.message()
                                              + "(Contract ID:" + c.getCommissionId() + ")");
        }
    }

    private void rollback(ITraderData conn) {
        try {
            conn.rollback();
        }
        catch (DataSourceException ex) {
            callOnException(new EngineRuntimeException(
                    Exceptions.DS_FAILURE_UNFIXABLE.code(),
                    Exceptions.DS_FAILURE_UNFIXABLE.message(),
                    ex));
        }
    }

    private class FrozenBundle {

        private Commission commission;
        private final Contract contract;
        private final Margin margin;

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
