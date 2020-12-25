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
import com.openglobes.core.event.EventSource;
import com.openglobes.core.event.EventSourceException;
import com.openglobes.core.event.IEvent;
import com.openglobes.core.event.IEventSource;
import com.openglobes.core.exceptions.EngineException;
import com.openglobes.core.exceptions.EngineRuntimeException;
import com.openglobes.core.exceptions.Exceptions;
import com.openglobes.core.exceptions.GatewayException;
import com.openglobes.core.exceptions.ServiceRuntimeStatus;
import com.openglobes.core.utils.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;

public class TraderEngine implements ITraderEngine {

    private ITraderEngineAlgorithm algo;
    private ITraderDataSource ds;
    private IEventSource es;
    private final IEventSource es0;
    private final Properties globalStartProps;
    private final HashMap<String, Instrument> instruments;
    private final HashMap<Long, Integer> orderTraders;
    private ServiceRuntimeStatus status;
    private final HashMap<Integer, TraderContext> traders;

    public TraderEngine() {
        traders = new HashMap<>(32);
        orderTraders = new HashMap<>(1024);
        instruments = new HashMap<>(512);
        globalStartProps = new Properties();
        es0 = new EventSource();
        setRequestHandler();
    }

    @Override
    public void enableTrader(int traderId, boolean enabled) throws EngineException {
        var i = getTraderGatewayContext(traderId);
        i.setEnabled(enabled);
    }

    @Override
    public ITraderEngineAlgorithm getAlgorithm() {
        return algo;
    }

    @Override
    public void setAlgorithm(ITraderEngineAlgorithm algo) throws EngineException {
        if (algo == null) {
            throw new EngineException(Exceptions.ALGORITHM_NULL.code(),
                                      Exceptions.ALGORITHM_NULL.message());
        }
        this.algo = algo;
    }

    @Override
    public ITraderDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(ITraderDataSource dataSource) throws EngineException {
        if (dataSource == null) {
            throw new EngineException(Exceptions.DATASOURCE_NULL.code(),
                                      Exceptions.DATASOURCE_NULL.message());
        }
        ds = dataSource;
    }

    @Override
    public IEventSource getEventSource() throws EngineException {
        return es;
    }

    @Override
    public void setEventSource(IEventSource eventSource) throws EngineException {
        if (eventSource == null) {
            throw new EngineException(Exceptions.EVENTSOURCE_NULL.code(),
                                      Exceptions.EVENTSOURCE_NULL.message());
        }
        es = eventSource;
    }

    @Override
    public Instrument getRelatedInstrument(String instrumentId) throws EngineException {
        return instruments.get(instrumentId);
    }

    @Override
    public ServiceRuntimeStatus getStatus() {
        return status;
    }

    @Override
    public TraderGatewayContext getTraderGatewayContext(int traderId) throws EngineException {
        return findContextByTraderId(traderId).getTraderGatewayContext();
    }

    @Override
    public Collection<TraderGatewayContext> getTraderGatewayContexts() throws EngineException {
        var r = new HashSet<TraderGatewayContext>(64);
        traders.values().forEach(c -> {
            r.add(c.getTraderGatewayContext());
        });
        return r;
    }

    @Override
    public void settle(Properties properties) throws EngineException {
        changeStatus(TraderEngineStatuses.SETTLING);
        check0();
        settle(ds, algo);
        try {
            settleAccount();
        }
        catch (EngineException e) {
            changeStatus(TraderEngineStatuses.SETTLE_FAILED);
            throw e;
        }
    }

    @Override
    public void initialize(Properties properties) throws EngineException {
        changeStatus(TraderEngineStatuses.INITIALIZING);
        check0();
        try {
            initializeAccount();
        }
        catch (EngineException e) {
            changeStatus(TraderEngineStatuses.INIT_FAILED);
            throw e;
        }
    }

    @Override
    public void registerTrader(int traderId, ITraderGateway trader) throws EngineException {
        if (trader == null) {
            throw new EngineException(Exceptions.TRADER_GATEWAY_NULL.code(),
                                      Exceptions.TRADER_GATEWAY_NULL.message());
        }
        if (traders.containsKey(traderId)) {
            throw new EngineException(Exceptions.TRADER_ID_DUPLICATED.code(),
                                      Exceptions.TRADER_ID_DUPLICATED.message());
        }
        addTrader(traderId, trader);
    }

    @Override
    public void request(Request request,
                        Instrument instrument,
                        Properties properties,
                        int requestId) throws EngineException {
        if (request == null) {
            throw new EngineException(Exceptions.REQUEST_NULL.code(),
                                      Exceptions.REQUEST_NULL.message());
        }
        try {
            es0.publish(RequestContext.class,
                        new RequestContext(request,
                                           instrument,
                                           properties,
                                           requestId));
        }
        catch (EventSourceException ex) {
            throw new EngineException(Exceptions.EVENT_PUBLISH_FAIL.code(),
                                      Exceptions.EVENT_PUBLISH_FAIL.message(),
                                      ex);
        }
    }

    @Override
    public void setInitProperties(int traderId, Properties properties) throws EngineException {
        getTraderGatewayContext(traderId).setInitProperties(properties);
    }

    @Override
    public void setSettleProperties(int traderId, Properties properties) throws EngineException {
        getTraderGatewayContext(traderId).setSettleProperties(properties);
    }

    @Override
    public void setStartProperties(int traderId, Properties properties) throws EngineException {
        getTraderGatewayContext(traderId).setStartProperties(properties);
    }

    @Override
    public void start(Properties properties) throws EngineException {
        changeStatus(TraderEngineStatuses.STARTING);
        try {
            globalStartProps.clear();
            if (properties != null) {
                globalStartProps.putAll(properties);
            }
            for (var p : traders.entrySet()) {
                startEach(p.getKey(), p.getValue());
            }
            changeStatus(TraderEngineStatuses.WORKING);
        }
        catch (EngineException e) {
            changeStatus(TraderEngineStatuses.START_FAILED);
            throw e;
        }
        catch (Throwable th) {
            changeStatus(TraderEngineStatuses.START_FAILED);
            throw new EngineException(Exceptions.UNEXPECTED_ERROR.code(),
                                      Exceptions.UNEXPECTED_ERROR.message(),
                                      th);
        }
    }

    @Override
    public void stop() throws EngineException {
        changeStatus(TraderEngineStatuses.STOPPING);
        try {
            for (var p : traders.entrySet()) {
                stopEach(p.getKey(), p.getValue());
            }
            changeStatus(TraderEngineStatuses.STOPPED);
        }
        catch (EngineException e) {
            changeStatus(TraderEngineStatuses.STOP_FAILED);
            throw e;
        }
        catch (Throwable th) {
            changeStatus(TraderEngineStatuses.STOP_FAILED);
            throw new EngineException(Exceptions.UNEXPECTED_ERROR.code(),
                                      Exceptions.UNEXPECTED_ERROR.message(),
                                      th);
        }
    }

    @Override
    public void unregisterTrader(int traderId) throws EngineException {
        /*
         * Verify trader with specified ID exists, or throw exception.
         */
        getTraderGatewayContext(traderId);
        traders.remove(traderId);
    }

    private void addTrader(int traderId, ITraderGateway trader) {
        var c = new TraderGatewayContext();
        c.setEnabled(false);
        c.setEngine(this);
        c.setTrader(trader);
        c.setTraderId(traderId);
        traders.put(traderId, new TraderContext(c));
    }

    private ServiceRuntimeStatus buildStatus(TraderEngineStatuses enums) {
        return new ServiceRuntimeStatus(enums.code(), enums.message());
    }

    /*
     * If something is wrong, tell user to handle it. If the handling is wrong,
     * tell user the handling is wrong.
     */
    private void callOnException(EngineRuntimeException e) {
        publishEvent(EngineRuntimeException.class, e);
    }

    private void callOnStatusChange(ServiceRuntimeStatus s) {
        publishEvent(ServiceRuntimeStatus.class, s);
    }

    private boolean canClose(Contract c, Request request) throws EngineException {
        if (c.getStatus() != ContractStatus.OPEN) {
            return false;
        }
        var offset = request.getOffset();
        var direction = request.getDirection();
        if (null == offset) {
            throw new EngineException(Exceptions.OFFSET_NULL.code(),
                                      Exceptions.OFFSET_NULL.message());
        }
        if (null == direction) {
            throw new EngineException(Exceptions.DIRECTION_NULL.code(),
                                      Exceptions.DIRECTION_NULL.message());
        }
        if (direction == Direction.BUY) {
            return c.getDirection() == Direction.SELL;
        }
        else {
            return c.getDirection() == Direction.BUY;
        }
    }

    private void changeStatus(TraderEngineStatuses enums) {
        this.status = buildStatus(enums);
        callOnStatusChange(this.status);
    }

    private void check0() throws EngineException {
        if (ds == null) {
            throw new EngineException(Exceptions.DATASOURCE_NULL.code(),
                                      Exceptions.DATASOURCE_NULL.message());
        }
        if (algo == null) {
            throw new EngineException(Exceptions.ALGORITHM_NULL.code(),
                                      Exceptions.ALGORITHM_NULL.message());
        }
    }

    private void check1(Integer key, TraderContext rt) throws EngineException {
        if (rt == null) {
            throw new EngineException(
                    Exceptions.TRADER_ID_NOT_FOUND.code(),
                    Exceptions.TRADER_ID_NOT_FOUND.message() + "(Trader ID:" + key.toString() + ")");
        }
    }

    private void check2(Instrument instrument) throws EngineException {
        if (instrument == null) {
            throw new EngineException(Exceptions.INSTRUMENT_NULL.code(),
                                      Exceptions.INSTRUMENT_NULL.message());
        }
    }

    private Collection<Contract> checkAssetsClose(Request request, Instrument instrument) throws EngineException {
        checkVolumn(request.getQuantity());
        var cs = getAvailableContracts(request);
        if (cs.size() < request.getQuantity()) {
            throw new EngineException(Exceptions.INSUFFICIENT_POSITION.code(),
                                      Exceptions.INSUFFICIENT_POSITION.message());
        }
        var r = new HashSet<Contract>(32);
        var c = algo.getCommission(request.getPrice(),
                               instrument,
                               request.getDirection(),
                               request.getOffset());
        for (int i = 0; i < request.getQuantity(); ++i) {
            var ctr = cs.get(i);
            r.add(ctr);
            setFrozenClose(c, ctr);
        }
        return r;
    }

    private void checkAssetsOpen(Request request, Instrument instrument) throws EngineException {
        checkVolumn(request.getQuantity());
        var a = algo.getAmount(request.getPrice(), instrument);
        var m = algo.getMargin(request.getPrice(), instrument);
        var c = algo.getCommission(request.getPrice(),
                               instrument,
                               request.getDirection(),
                               request.getOffset());
        var total = request.getQuantity() * (m + c);
        var available = getAvailableMoney();
        if (available < total) {
            throw new EngineException(Exceptions.INSUFFICIENT_MONEY.code(),
                                      Exceptions.INSUFFICIENT_MONEY.message());
        }
        for (int i = 0; i < request.getQuantity(); ++i) {
            setFrozenOpen(a, m, c, request);
        }
    }

    private void checkVolumn(Long v) throws EngineException {
        if (v == null) {
            throw new EngineException(Exceptions.VOLUMN_NULL.code(),
                                      Exceptions.VOLUMN_NULL.message());
        }
        if (v <= 0) {
            throw new EngineException(Exceptions.NONPOSITIVE_VOLUMN.code(),
                                      Exceptions.NONPOSITIVE_VOLUMN.message());
        }
    }

    private void clearInternals() {
        orderTraders.clear();
        instruments.clear();
        traders.values().forEach(s -> {
            s.clear();
        });
    }

    private void decideTrader(Request request) throws EngineException {
        var rt = getProperTrader(request);
        if (!Objects.equals(rt.getTraderId(), request.getTraderId())) {
            request.setTraderId(rt.getTraderId());
        }
    }

    private void deleteOrderRequest(Request request) throws EngineException {
        var orderId = request.getOrderId();
        var traderId = findTraderIdByOrderId(orderId);
        var ctx = findContextByTraderId(traderId);
        var h = ctx.getHandler();
        if (h == null) {
            throw new EngineException(Exceptions.TRADER_GW_HANDLER_NULL.code(),
                                      Exceptions.TRADER_GW_HANDLER_NULL.message());
        }
        var r = new Response();
        r.setInstrumentId(request.getInstrumentId());
        r.setOrderId(orderId);
        r.setTraderId(traderId);
        r.setAction(ActionType.DELETE);
        r.setOffset(request.getOffset());
        r.setDirection(request.getDirection());
        r.setTradingDay(ctx.getGatewayInfo().getTradingDay());
        r.setSignature(Utils.nextUuid().toString());
        r.setStatusCode(0);

        try {
            h.onResponse(r);
        }
        catch (Throwable th) {
            callOnException(new EngineRuntimeException(Exceptions.DELETE_ORDER_FAILED.code(),
                                                       Exceptions.DELETE_ORDER_FAILED.message(),
                                                       th));
        }
    }

    private void deleteRequest(Request request,
                               TraderContext context,
                               int requestId) throws EngineException {
        var ids = context.getDestinatedIds(request.getOrderId());
        if (ids == null) {
            throw new EngineException(Exceptions.DEST_ID_NOT_FOUND.code(),
                                      Exceptions.DEST_ID_NOT_FOUND.message()
                                      + "(Source order ID:" + request.getOrderId() + ")");
        }
        for (var i : ids) {
            /*
             * If the order is fulfilled, don't cancel it any more.
             */
            var cd = context.getDownCountByDestId(i);
            if (cd == null) {
                throw new EngineException(
                        Exceptions.COUNTDOWN_NOT_FOUND.code(),
                        Exceptions.COUNTDOWN_NOT_FOUND.message() + "(Destinated ID: " + i + ")");
            }
            if (cd <= 0) {
                continue;
            }
            var c = Utils.copy(request);
            c.setOrderId(i);
            try {
                context.insert(c, requestId);
            }
            catch (GatewayException ex) {
                throw new EngineException(ex.getCode(),
                                          ex.getMessage(),
                                          ex);
            }
        }
    }

    private void dispatchRequest(RequestContext ctx) {
        try {
            if (ctx.getRequest().getAction() == ActionType.DELETE) {
                forDelete(ctx.getRequest(),
                          ctx.getRequestId());
            }
            else {
                forNew(ctx.getRequest(),
                       ctx.getInstrument(),
                       ctx.getProperties(),
                       ctx.getRequestId());
            }
        }
        catch (EngineException e) {
            callOnException(new EngineRuntimeException(Exceptions.REQUEST_DISPATCH_FAIL.code(),
                                                       Exceptions.REQUEST_DISPATCH_FAIL.message(),
                                                       e));
        }
    }

    private TraderContext findContextByTraderId(int traderId) throws EngineException {
        var rt = traders.get(traderId);
        check1(traderId, rt);
        return rt;
    }

    private TraderContext findContextRandomly(Request request) throws EngineException {
        var a = new ArrayList<>(traders.keySet());
        traders.forEach((k, v) -> {
            if (v.isEnabled()) {
                a.add(k);
            }
        });
        if (a.isEmpty()) {
            throw new EngineException(Exceptions.NO_TRADER.code(),
                                      Exceptions.NO_TRADER.message());
        }
        var traderId = a.get(new Random().nextInt(a.size()));
        orderTraders.put(request.getOrderId(), traderId);
        return traders.get(traderId);
    }

    private Map<String, Instrument> findRelatedInstruments(Collection<String> instrumentIds,
                                                           ITraderData conn) throws EngineException {
        final var r = new HashMap<String, Instrument>(512);
        for (var i : instrumentIds) {
            var instrument = conn.getInstrumentById(i);
            if (instrument == null) {
                throw new EngineException(Exceptions.INSTRUMENT_NULL.code(),
                                          Exceptions.INSTRUMENT_NULL.message() + "(" + i + ")");
            }
            r.put(i, instrument);
        }
        return r;
    }

    private Map<String, SettlementPrice> findRelatedTicks(Collection<String> instrumentIds,
                                                          ITraderData conn) throws EngineException {
        final var r = new HashMap<String, SettlementPrice>(512);
        for (var i : instrumentIds) {
            var price = conn.getSettlementPriceByInstrumentId(i);
            if (price == null) {
                throw new EngineException(Exceptions.TICK_NULL.code(),
                                          Exceptions.TICK_NULL.message() + "(" + i + ")");
            }
            r.put(i, price);
        }
        return r;
    }

    private Integer findTraderIdByOrderId(long orderId) throws EngineException {
        var traderId = orderTraders.get(orderId);
        if (traderId == null) {
            throw new EngineException(Exceptions.ORDER_ID_NOT_FOUND.code(),
                                      Exceptions.ORDER_ID_NOT_FOUND.message() + "(Order ID:" + orderId + ")");
        }
        return traderId;
    }

    private void forDelete(Request request, int requestId) throws EngineException {
        if (request == null) {
            throw new EngineException(Exceptions.DELETE_REQS_NULL.code(),
                                      Exceptions.DELETE_REQS_NULL.message());
        }
        forwardRequest(request, request.getTraderId(), requestId);
    }

    private void forNew(Request request,
                        Instrument instrument,
                        Properties properties,
                        int requestId) throws EngineException {
        check0();
        check2(instrument);
        /*
         * Remmeber the instrument it once operated.
         */
        instruments.put(instrument.getInstrumentId(), instrument);
        if (request.getOffset() == Offset.OPEN) {
            decideTrader(request);
            checkAssetsOpen(request, instrument);
            forwardRequest(request, request.getTraderId(), requestId);
        }
        else {
            var cs = checkAssetsClose(request, instrument);
            for (var r : group(cs, request)) {
                forwardRequest(r, r.getTraderId(), requestId);
            }
        }
    }

    private void forwardRequest(Request request, Integer traderId, int requestId) throws EngineException {
        var ctx = findContextByTraderId(traderId);
        check1(traderId, ctx);
        if (null == request.getAction()) {
            throw new EngineException(Exceptions.ACTION_NULL.code(),
                                      Exceptions.ACTION_NULL.message());
        }
        if (request.getAction() == ActionType.NEW) {
            newRequest(request, ctx, requestId);
        }
        else {
            deleteRequest(request, ctx, requestId);
        }
    }

    private List<Contract> getAvailableContracts(Request request) throws EngineException {
        final var conn = ds.getConnection();
        final var cs = conn.getContractsByInstrumentId(request.getInstrumentId());
        if (cs == null) {
            throw new EngineException(Exceptions.CONTRACT_NULL.code(),
                                      Exceptions.CONTRACT_NULL.message());
        }
        var sorted = new LinkedList<Contract>(cs);
        sorted.sort((Contract o1, Contract o2)
                -> o1.getOpenTimestamp().compareTo(o2.getOpenTimestamp()));
        // Scan from earlier to later.
        var it = sorted.iterator();
        while (it.hasNext()) {
            var c = it.next();
            if (!canClose(c, request)) {
                it.remove();
            }
        }
        return sorted;
    }

    private double getAvailableMoney() throws EngineException {
        var a = getSettledAccount();
        return (a.getBalance() - a.getMargin() - a.getFrozenMargin() - a.getFrozenCommission());
    }

    private Collection<Contract> getContractsByOrderResponses(Collection<Trade> rsps) throws EngineException {
        final var cs = new HashSet<Contract>(128);
        final var conn = ds.getConnection();
        for (var r : rsps) {
            var s = conn.getContractsByTradeId(r.getTradeId());
            if (s == null) {
                throw new EngineException(Exceptions.NO_CONTRACT.code(),
                                          Exceptions.NO_CONTRACT.message()
                                          + "(Trade ID:" + r.getTradeId() + ")");
            }
            cs.addAll(s);
        }
        return cs;
    }

    private TraderContext getProperTrader(Request request) throws EngineException {
        var traderId = request.getTraderId();
        if (traderId == null) {
            return findContextRandomly(request);
        }
        else {
            var ctx = findContextByTraderId(traderId);
            check1(traderId, ctx);
            if (!ctx.isEnabled()) {
                throw new EngineException(Exceptions.TRADER_NOT_ENABLED.code(),
                                          Exceptions.TRADER_NOT_ENABLED.message() + "(Trader ID:" + traderId + ")");
            }
            orderTraders.put(request.getOrderId(), traderId);
            return ctx;
        }
    }

    private Collection<String> getRalatedInstrumentIds() {
        return null;
    }

    private Account getSettledAccount() throws EngineException {
        final var conn = ds.getConnection();
        final var tradingDay = conn.getTradingDay().getTradingDay();
        final var ids = getRalatedInstrumentIds();
        return algo.getAccount(conn.getAccount(),
                               conn.getDeposits(),
                               conn.getWithdraws(),
                               algo.getPositions(conn.getContracts(),
                                                 conn.getCommissions(),
                                                 conn.getMargins(),
                                                 findRelatedTicks(ids, conn),
                                                 findRelatedInstruments(ids, conn),
                                                 tradingDay));
    }

    private Collection<Request> group(Collection<Contract> cs, Request request) throws EngineException {
        final var today = new HashMap<Integer, Request>(64);
        final var yd = new HashMap<Integer, Request>(64);
        var tradingDay = ds.getConnection().getTradingDay().getTradingDay();
        for (var c : cs) {
            if (c.getOpenTradingDay().isBefore(tradingDay)) {
                var o = yd.computeIfAbsent(c.getTraderId(), k -> {
                                   var co = Utils.copy(request);
                                   if (co == null) {
                                       throw new EngineRuntimeException(
                                               Exceptions.OBJECT_COPY_FAILED.code(),
                                               Exceptions.OBJECT_COPY_FAILED.message());
                                   }
                                   co.setOffset(Offset.CLOSE);
                                   co.setQuantity(0L);
                                   co.setTraderId(k);
                                   return co;

                               });
                o.setQuantity(o.getQuantity() + 1);
            }
            else {
                var o = today.computeIfAbsent(c.getTraderId(), k -> {
                                      var co = Utils.copy(request);
                                      if (co == null) {
                                          throw new EngineRuntimeException(
                                                  Exceptions.OBJECT_COPY_FAILED.code(),
                                                  Exceptions.OBJECT_COPY_FAILED.message());
                                      }
                                      co.setOffset(Offset.CLOSE_TODAY);
                                      co.setQuantity(0L);
                                      co.setTraderId(k);
                                      return co;

                                  });
                o.setQuantity(o.getQuantity() + 1);
            }
        }

        var r = new HashSet<Request>(today.values());
        r.addAll(yd.values());
        return r;
    }

    private void initAccount(Account a) throws EngineException {
        if (a == null) {
            throw new EngineException(Exceptions.ACCOUNT_NULL.code(),
                                      Exceptions.ACCOUNT_NULL.message());
        }
        final var conn = ds.getConnection();
        final var tradingDay = conn.getTradingDay().getTradingDay();

        a.setPreBalance(a.getBalance());
        a.setPreDeposit(a.getDeposit());
        a.setPreMargin(a.getMargin());
        a.setPreWithdraw(a.getWithdraw());
        a.setBalance(0.0D);
        a.setDeposit(0.0D);
        a.setMargin(0.0D);
        a.setWithdraw(0.0D);
        a.setTradingDay(tradingDay);

        conn.updateAccount(a);
    }

    private void initContracts(Collection<Contract> cs, ITraderData conn) throws EngineException {
        if (cs == null) {
            throw new EngineException(Exceptions.CONTRACT_NULL.code(),
                                      Exceptions.CONTRACT_NULL.message());
        }
        for (var c : cs) {
            conn.removeContract(c.getContractId());
        }
    }

    private void initWithdrawDeposit(Collection<Withdraw> ws,
                                     Collection<Deposit> ds,
                                     ITraderData conn) throws EngineException {
        if (ws == null) {
            throw new EngineException(Exceptions.WITHDRAW_NULL.code(),
                                      Exceptions.WITHDRAW_NULL.message());
        }
        if (ds == null) {
            throw new EngineException(Exceptions.DEPOSIT_NULL.code(),
                                      Exceptions.DEPOSIT_NULL.message());
        }
        for (var w : ws) {
            conn.removeMargin(w.getWithdrawId());
        }
        for (var d : ds) {
            conn.removeDeposit(d.getDepositId());
        }
    }

    private void initializeAccount() throws EngineException {
        ITraderData conn = null;
        try {
            conn = ds.getConnection();
            conn.transaction();
            initAccount(conn.getAccount());
            initContracts(conn.getContractsByStatus(ContractStatus.CLOSED), conn);
            initWithdrawDeposit(conn.getWithdraws(),
                                conn.getDeposits(),
                                conn);
            conn.commit();
            changeStatus(TraderEngineStatuses.WORKING);
        }
        catch (DataSourceException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        }
        catch (Throwable th) {
            throw new EngineException(Exceptions.UNEXPECTED_ERROR.code(),
                                      Exceptions.UNEXPECTED_ERROR.message(),
                                      th);
        }
    }

    private void newRequest(Request request,
                            TraderContext context,
                            int requestId) throws EngineException {
        var destId = context.getDestinatedId(request.getOrderId(), request.getQuantity());
        request.setOrderId(destId);
        try {
            context.insert(request, requestId);
        }
        catch (GatewayException ex) {
            throw new EngineException(ex.getCode(),
                                      ex.getMessage(),
                                      ex);
        }
    }

    private void setFrozenClose(double commission,
                                Contract contract) throws EngineException {
        ITraderData conn = null;
        try {
            conn = ds.getConnection();
            final var tradingDay = conn.getTradingDay().getTradingDay();
            conn.transaction();
            /*
             * Update contracts status to make it frozen.
             */
            contract.setStatus(ContractStatus.CLOSING);
            conn.updateContract(contract);
            /*
             * Add new commission for the current order, and make it frozen
             * before order is filled.
             */
            var cms = new Commission();
            cms.setCommission(commission);
            cms.setCommissionId(Utils.nextId());
            cms.setContractId(contract.getContractId());
            cms.setStatus(FeeStatus.FORZEN);
            cms.setTradingDay(tradingDay);
            conn.addCommission(cms);
            /*
             * Commit change.
             */
            conn.commit();
        }
        catch (EngineException e) {
            /*
             * Rollback data source.
             */
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        }
    }

    private void setFrozenOpen(double amount,
                               double margin,
                               double commission,
                               Request request) throws EngineException {
        ITraderData conn = null;
        try {
            conn = ds.getConnection();
            final var tradingDay = conn.getTradingDay().getTradingDay();
            conn.transaction();
            /*
             * Add preparing contract.
             */
            var ctr = new Contract();
            ctr.setContractId(Utils.nextId());
            ctr.setTraderId(request.getTraderId());
            ctr.setInstrumentId(request.getInstrumentId());
            ctr.setOpenAmount(amount);
            ctr.setOpenTradingDay(tradingDay);
            ctr.setDirection(request.getDirection());
            ctr.setStatus(ContractStatus.OPENING);
            conn.addContract(ctr);
            /*
             * Add frozen margin.
             */
            var cmn = new Commission();
            cmn.setCommission(commission);
            cmn.setCommissionId(Utils.nextId());
            cmn.setContractId(ctr.getContractId());
            cmn.setOrderId(request.getOrderId());
            cmn.setStatus(FeeStatus.FORZEN);
            cmn.setTradingDay(tradingDay);
            conn.addCommission(cmn);
            /*
             * Add frozen commission.
             */
            var mn = new Margin();
            mn.setContractId(ctr.getContractId());
            mn.setMargin(margin);
            mn.setMarginId(Utils.nextId());
            mn.setOrderId(request.getOrderId());
            mn.setStatus(FeeStatus.FORZEN);
            mn.setTradingDay(tradingDay);
            conn.addMargin(mn);
            /*
             * Commit change.
             */
            conn.commit();
        }
        catch (EngineException e) {
            /*
             * Rollback on exception.
             */
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        }
    }

    private void setRequestHandler() {
        try {
            es0.subscribe(RequestContext.class, (IEvent<RequestContext> event) -> {
                      dispatchRequest(event.get());
                  });
            es0.start();
        }
        catch (EventSourceException ignored) {
        }
    }

    private void settle(ITraderDataSource ds, ITraderEngineAlgorithm algo) throws EngineException {
        final var conn = ds.getConnection();
        var rs = conn.getRequests();
        if (rs == null) {
            throw new EngineException(Exceptions.REQUEST_NULL.code(),
                                      Exceptions.REQUEST_NULL.message());
        }
        for (var r : rs) {
            var orderId = r.getOrderId();
            if (orderId == null) {
                throw new EngineException(Exceptions.ORDER_ID_NULL.code(),
                                          Exceptions.ORDER_ID_NULL.message());
            }
            var trades = conn.getTradesByOrderId(orderId);
            if (trades == null) {
                throw new EngineException(Exceptions.NO_TRADE.code(),
                                          Exceptions.NO_TRADE.message());
            }
            var ctrs = getContractsByOrderResponses(trades);
            if (ctrs == null) {
                throw new EngineException(Exceptions.NO_CONTRACT.code(),
                                          Exceptions.NO_CONTRACT.message());

            }
            var cals = conn.getResponseByOrderId(orderId);
            if (cals == null) {
                throw new EngineException(Exceptions.NO_RESPONSE.code(),
                                          Exceptions.NO_RESPONSE.message());

            }
            var o = algo.getOrder(r, ctrs, trades, cals);
            var s = o.getStatus();
            if (s == OrderStatus.ACCEPTED
                || s == OrderStatus.QUEUED
                || s == OrderStatus.UNQUEUED) {
                deleteOrderRequest(r);
            }
        }
        // Clear everyday to avoid mem leak.
        clearInternals();
    }

    private void settleAccount() throws EngineException {
        ITraderData conn = null;
        try {
            /*
             * Update settlement into db.
             */
            conn = ds.getConnection();
            conn.transaction();
            conn.updateAccount(getSettledAccount());
            conn.commit();
            changeStatus(TraderEngineStatuses.WORKING);
        }
        catch (DataSourceException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        }
        catch (Throwable th) {
            throw new EngineException(Exceptions.UNEXPECTED_ERROR.code(),
                                      Exceptions.UNEXPECTED_ERROR.message(),
                                      th);
        }
    }

    private void startEach(Integer key, TraderContext context) throws EngineException {
        check1(key, context);
        if (!context.isEnabled()) {
            return;
        }
        /*
         * Trader services share the same class of handler, not same instance of
         * handler. To shared information among these handlers, use STATIC.
         */
        if (context.getHandler() == null) {
            context.setHandler(new TraderGatewayHandler(context));
        }
        try {
            context.start(globalStartProps);
        }
        catch (GatewayException ex) {
            throw new EngineException(
                    ex.getCode(),
                    ex.getMessage() + "(Trader ID:" + key.toString() + ")",
                    ex);

        }
    }

    private void stopEach(Integer key, TraderContext context) throws EngineException {
        check1(key, context);
        try {
            context.stop();
        }
        catch (GatewayException ex) {
            throw new EngineException(
                    ex.getCode(),
                    ex.getMessage() + "(Trader ID:" + key.toString() + ")",
                    ex);
        }
    }

    <T> void publishEvent(Class<T> clazz, T object) {
        if (es == null || es.isEmpty()) {
            return;
        }
        if (!es.isStarted()) {
            throw new EngineRuntimeException(Exceptions.PUBLISH_TO_STOPPED_QUEUE.code(),
                                             Exceptions.PUBLISH_TO_STOPPED_QUEUE.message());
        }
        try {
            es.publish(clazz, object);
        }
        catch (EventSourceException ex) {
            throw new EngineRuntimeException(Exceptions.EVENT_PUBLISH_FAIL.code(),
                                             Exceptions.EVENT_PUBLISH_FAIL.message(),
                                             ex);
        }
    }

}
