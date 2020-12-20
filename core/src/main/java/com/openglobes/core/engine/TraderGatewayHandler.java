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
package com.openglobes.core.engine;

import com.openglobes.core.ActionType;
import com.openglobes.core.Commission;
import com.openglobes.core.Contract;
import com.openglobes.core.ContractStatus;
import com.openglobes.core.FeeStatus;
import com.openglobes.core.Instrument;
import com.openglobes.core.Margin;
import com.openglobes.core.Offset;
import com.openglobes.core.Request;
import com.openglobes.core.Response;
import com.openglobes.core.Trade;
import com.openglobes.core.exceptions.EngineException;
import com.openglobes.core.exceptions.EngineRuntimeException;
import com.openglobes.core.exceptions.GatewayException;
import com.openglobes.core.exceptions.GatewayRuntimeException;
import com.openglobes.core.gateway.ITraderGatewayHandler;
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
public class TraderGatewayHandler extends IdTranslator implements ITraderGatewayHandler {

    private final TraderGatewayRuntime info;

    public TraderGatewayHandler(TraderGatewayRuntime info) {
        this.info = info;
    }

    @Override
    public void onException(GatewayRuntimeException exception) {
        var handlers = info.getEngine().handlers();
        if (handlers.isEmpty()) {
            return;
        }
        handlers.parallelStream().forEach(h -> {
            try {
                h.onException(new EngineRuntimeException(exception.getCode(),
                                                         exception.getMessage(),
                                                         exception));
            }
            catch (Throwable th) {
                callOnException(new EngineRuntimeException(ExceptionCodes.USER_CODE_ERROR.code(),
                                                           ExceptionCodes.USER_CODE_ERROR.message(),
                                                           th));
            }
        });
    }

    @Override
    public void onException(Request request,
                            GatewayRuntimeException exception,
                            int requestId) {
        if (request.getAction() == ActionType.DELETE) {
            callOnDeleteException(request,
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
            info.getEngine().getDataSource().getConnection().addResponse(response);
            preprocess(response);
            if (response.getAction() == ActionType.DELETE) {
                dealDelete(response);
            }
            callOnResponse(response);
        }
        catch (DataSourceException ex) {
            callOnException(new EngineRuntimeException(ExceptionCodes.DS_FAILURE_UNFIXABLE.code(),
                                                       "Fail saving response to data source.",
                                                       ex));
        }
        catch (EngineException ex) {
            callOnException(new EngineRuntimeException(ExceptionCodes.PREPROCESS_RESPONSE_FAIL.code(),
                                                       ExceptionCodes.PREPROCESS_RESPONSE_FAIL.message(),
                                                       ex));
        }
    }

    @Override
    public void onStatusChange(int status) {
        var handlers = info.getEngine().handlers();
        if (handlers.isEmpty()) {
            return;
        }
        handlers.parallelStream().forEach(h -> {
            try {
                h.onTraderServiceStatusChange(status);
            }
            catch (Throwable th) {
                callOnException(new EngineRuntimeException(ExceptionCodes.USER_CODE_ERROR.code(),
                                                           ExceptionCodes.USER_CODE_ERROR.message(),
                                                           th));
            }
        });
    }

    @Override
    public void onTrade(Trade trade) {
        try {
            preprocess(trade);
            dealTrade(trade);
            callOnTrade(trade);
        }
        catch (EngineException ex) {
            callOnException(new EngineRuntimeException(ExceptionCodes.PREPROCESS_TRADE_FAIL.code(),
                                                       ExceptionCodes.PREPROCESS_TRADE_FAIL.message(),
                                                       ex));
        }
    }

    private void callOnDeleteException(Request request,
                                       GatewayRuntimeException exception,
                                       int requestId) {
        var handlers = info.getEngine().handlers();
        if (handlers.isEmpty()) {
            return;
        }
        handlers.parallelStream().forEach(h -> {
            try {
                h.onException(request,
                              new EngineRuntimeException(exception.getCode(),
                                                         exception.getMessage(),
                                                         exception),
                              requestId);
            }
            catch (Throwable th) {
                callOnException(new EngineRuntimeException(ExceptionCodes.USER_CODE_ERROR.code(),
                                                           ExceptionCodes.USER_CODE_ERROR.message(),
                                                           th));
            }
        });
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

    private void callOnRequestException(Request request,
                                        GatewayRuntimeException exception,
                                        int requestId) {
        var handlers = info.getEngine().handlers();
        if (handlers.isEmpty()) {
            return;
        }
        handlers.parallelStream().forEach(h -> {
            try {
                h.onException(request,
                              new EngineRuntimeException(exception.getCode(),
                                                         exception.getMessage(),
                                                         exception),
                              requestId);
            }
            catch (Throwable th) {
                callOnException(new EngineRuntimeException(ExceptionCodes.USER_CODE_ERROR.code(),
                                                           ExceptionCodes.USER_CODE_ERROR.message(),
                                                           th));
            }
        });
    }

    private void callOnResponse(Response response) {
        var handlers = info.getEngine().handlers();
        if (handlers.isEmpty()) {
            return;
        }
        handlers.parallelStream().forEach(h -> {
            try {
                h.onResponse(response);
            }
            catch (Throwable th) {
                callOnException(new EngineRuntimeException(ExceptionCodes.USER_CODE_ERROR.code(),
                                                           ExceptionCodes.USER_CODE_ERROR.message(),
                                                           th));
            }
        });
    }

    private void callOnTrade(Trade trade) {
        var handlers = info.getEngine().handlers();
        if (handlers.isEmpty()) {
            return;
        }
        handlers.parallelStream().forEach(h -> {
            try {
                h.onTrade(trade);
            }
            catch (Throwable th) {
                callOnException(new EngineRuntimeException(ExceptionCodes.USER_CODE_ERROR.code(),
                                                           ExceptionCodes.USER_CODE_ERROR.message(),
                                                           th));
            }
        });
    }

    private void checkCommissionsNull(Collection<Commission> cs) {
        if (cs == null) {
            throw new GatewayRuntimeException(ExceptionCodes.COMMISSION_NULL.code(),
                                              ExceptionCodes.COMMISSION_NULL.message());
        }
    }

    private void checkContractIdNull(Long cid) {
        if (cid == null) {
            throw new GatewayRuntimeException(ExceptionCodes.CONTRACT_ID_NULL.code(),
                                              ExceptionCodes.CONTRACT_ID_NULL.message());
        }
    }

    private void checkContractNull(Contract cc) {
        if (cc == null) {
            throw new GatewayRuntimeException(ExceptionCodes.CONTRACT_NULL.code(),
                                              ExceptionCodes.CONTRACT_NULL.message());
        }
    }

    private void checkFrozenInfo(Collection<FrozenBundle> bs) {
        bs.forEach(v -> {
            if (v.getCommission() == null || v.getContract() == null || v.getMargin() == null) {
                throw new GatewayRuntimeException(ExceptionCodes.INCONSISTENT_FROZEN_INFO.code(),
                                                  ExceptionCodes.INCONSISTENT_FROZEN_INFO.message());
            }
        });
    }

    private void checkMarginNull(Margin n) {
        if (n == null) {
            throw new GatewayRuntimeException(ExceptionCodes.MARGIN_NULL.code(),
                                              ExceptionCodes.MARGIN_NULL.message());
        }
    }

    private void checkMarginsNull(Collection<Margin> cs) {
        if (cs == null) {
            throw new GatewayRuntimeException(ExceptionCodes.MARGIN_NULL.code(),
                                              ExceptionCodes.MARGIN_NULL.message());
        }
    }

    private void closeDelete(Response response, IDataConnection conn) throws DataSourceException {
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

    private void closeTrade(Trade trade, IDataConnection conn) throws EngineException {
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
            throw new EngineRuntimeException(ExceptionCodes.INCONSISTENT_FROZEN_INFO.code(),
                                             ExceptionCodes.INCONSISTENT_FROZEN_INFO.message());
        }
    }

    private void dealClose(Commission commission,
                           Margin margin,
                           Contract contract,
                           Trade response,
                           IDataConnection conn) throws EngineException {
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
        var amount = info.getEngine().getAlgorithm().getAmount(price, instrument);
        contract.setCloseAmount(amount);
        contract.setStatus(ContractStatus.CLOSED);
        conn.updateContract(contract);
    }

    private void dealDelete(Response response) throws DataSourceException {
        IDataConnection conn = null;
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
                throw new EngineRuntimeException(ExceptionCodes.ORDER_ID_NOT_FOUND.code(),
                                                 ExceptionCodes.ORDER_ID_NOT_FOUND.message());
            }
            var offset = o.getOffset();
            if (offset == null) {
                throw new EngineRuntimeException(ExceptionCodes.OFFSET_NULL.code(),
                                                 ExceptionCodes.OFFSET_NULL.message());
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
                          IDataConnection conn) throws EngineException {
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
        var amount = info.getEngine().getAlgorithm().getAmount(price, instrument);
        contract.setOpenAmount(amount);
        contract.setStatus(ContractStatus.OPEN);
        contract.setTradeId(trade.getTradeId());
        contract.setOpenTimestamp(trade.getTimestamp());
        contract.setOpenTradingDay(trade.getTradingDay());
        conn.updateCommission(commission);
    }

    private void dealTrade(Trade trade) throws EngineException {
        IDataConnection conn = null;
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
                throw new GatewayRuntimeException(ExceptionCodes.OFFSET_NULL.code(),
                                                  ExceptionCodes.OFFSET_NULL.message());
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
                             IDataConnection conn) throws DataSourceException {
        requireStatus(contract, ContractStatus.CLOSING);
        contract.setStatus(ContractStatus.OPEN);
        conn.updateContract(contract);
        conn.removeCommission(commission.getCommissionId());
    }

    private void deleteOpen(Commission commission,
                            Margin margin,
                            Contract contract,
                            IDataConnection conn) throws DataSourceException {
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
            callOnRequestException(request,
                                   exception,
                                   requestId);
        }
        catch (Throwable th) {
            callOnException(new EngineRuntimeException(ExceptionCodes.USER_CODE_ERROR.code(),
                                                       ExceptionCodes.USER_CODE_ERROR.message(),
                                                       th));
        }
    }

    private IDataSource getDataSource() throws GatewayException {
        var ds = info.getEngine().getDataSource();
        if (ds == null) {
            throw new GatewayException(ExceptionCodes.DATASOURCE_NULL.code(),
                                       ExceptionCodes.DATASOURCE_NULL.message());
        }
        return ds;
    }

    private Collection<FrozenBundle> getFrozenBundles(Long orderId, IDataConnection conn) throws DataSourceException {
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
        var instrument = info.getEngine().getRelatedInstrument(instrumentId);
        if (instrument == null) {
            throw new EngineException(
                    ExceptionCodes.INSTRUMENT_NULL.code(),
                    ExceptionCodes.INSTRUMENT_NULL.message() + "(Instrument ID:"
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
        var srcId = getSourceId(destId);
        if (srcId == null) {
            throw new NullPointerException("Source ID not found(Destinated ID:" + destId + ").");
        }
        return srcId;
    }

    private Response initResponse(Request request) {
        var r = new Response();
        r.setInstrumentId(request.getInstrumentId());
        r.setOrderId(request.getOrderId());
        r.setTraderId(request.getTraderId());
        r.setTradingDay(info.getTrader().getServiceInfo().getTradingDay());
        r.setUuid(Utils.nextUuid().toString());
        return r;
    }

    private void openDelete(Response response, IDataConnection conn) throws DataSourceException {
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

    private void openTrade(Trade trade, IDataConnection conn) throws EngineException {
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
            throw new EngineRuntimeException(ExceptionCodes.INCONSISTENT_FROZEN_INFO.code(),
                                             ExceptionCodes.INCONSISTENT_FROZEN_INFO.message());
        }
    }

    private void preprocess(Trade trade) throws EngineException {
        try {
            /*
             * Order is deleted, so count down to zero.
             */
            super.countDown(trade.getOrderId(), trade.getQuantity());
            trade.setOrderId(getSrcId(trade.getOrderId()));
            trade.setTraderId(info.getTraderId());
        }
        catch (Throwable th) {
            throw new EngineException(ExceptionCodes.PREPROC_RSPS_FAILED.code(),
                                      ExceptionCodes.PREPROC_RSPS_FAILED.message(),
                                      th);
        }
    }

    private void preprocess(Response response) throws EngineException {
        try {
            var rest = super.getDownCountByDestId(response.getOrderId());
            if (rest == null) {
                throw new NullPointerException("Count down not found(" + response.getOrderId() + ").");
            }
            /*
             * Order is deleted, so count down to zero.
             */
            super.countDown(response.getOrderId(), rest);
            response.setOrderId(getSrcId(response.getOrderId()));
            response.setTraderId(info.getTraderId());
        }
        catch (Throwable th) {
            throw new EngineException(ExceptionCodes.PREPROC_RSPS_FAILED.code(),
                                      ExceptionCodes.PREPROC_RSPS_FAILED.message(),
                                      th);
        }
    }

    private void requireStatus(Contract c, ContractStatus s) {
        if (!Objects.equals(c.getStatus(), s)) {
            throw new GatewayRuntimeException(ExceptionCodes.INVALID_DELETING_CONTRACT_STATUS.code(),
                                              ExceptionCodes.INVALID_DELETING_CONTRACT_STATUS.message()
                                              + "(Contract ID:" + c.getContractId() + ")");
        }
    }

    private void requireStatus(Margin m, FeeStatus s) {
        if (!Objects.equals(m.getStatus(), s)) {
            throw new GatewayRuntimeException(ExceptionCodes.INVALID_DELETING_MARGIN_STATUS.code(),
                                              ExceptionCodes.INVALID_DELETING_MARGIN_STATUS.message()
                                              + "(Contract ID:" + m.getMarginId() + ")");
        }
    }

    private void requireStatus(Commission c, FeeStatus s) {
        if (!Objects.equals(c.getStatus(), s)) {
            throw new GatewayRuntimeException(ExceptionCodes.INVALID_DELETING_COMMISSION_STATUS.code(),
                                              ExceptionCodes.INVALID_DELETING_COMMISSION_STATUS.message()
                                              + "(Contract ID:" + c.getCommissionId() + ")");
        }
    }

    private void rollback(IDataConnection conn) {
        try {
            conn.rollback();
        }
        catch (DataSourceException ex) {
            callOnException(new EngineRuntimeException(
                    ExceptionCodes.DS_FAILURE_UNFIXABLE.code(),
                    ExceptionCodes.DS_FAILURE_UNFIXABLE.message(),
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
