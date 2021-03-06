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

import com.openglobes.core.ServiceRuntimeStatus;
import com.openglobes.core.data.*;
import com.openglobes.core.event.*;
import com.openglobes.core.utils.Loggers;
import com.openglobes.core.utils.Utils;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class TraderEngine implements ITraderEngine {

    private final IEventSource es0;
    private final HashMap<String, Instrument> instruments;
    private final HashMap<Long, Integer> orderTraders;
    private final HashMap<Integer, TraderContext> traders;
    private ITraderEngineAlgorithm algo;
    private ITraderDataSource ds;
    private IEventSource es;
    private ServiceRuntimeStatus status;

    public TraderEngine() {
        traders = new HashMap<>(32);
        orderTraders = new HashMap<>(1024);
        instruments = new HashMap<>(512);
        es0 = new EventSource();
        try {
            es0.subscribe(RequestDetail.class, (IEvent<RequestDetail> event) -> {
                dispatchRequest(event.get());
            });
        } catch (InvalidSubscriptionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void enableTrader(int traderId, boolean enabled) throws UnknownTraderIdException {
        var i = getTraderGatewayContext(traderId);
        i.setEnabled(enabled);
    }

    @Override
    public ITraderEngineAlgorithm getAlgorithm() {
        return algo;
    }

    @Override
    public void setAlgorithm(ITraderEngineAlgorithm algo) {
        Objects.requireNonNull(algo);
        this.algo = algo;
    }

    @Override
    public ITraderDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(ITraderDataSource dataSource) {
        Objects.requireNonNull(ds);
        ds = dataSource;
    }

    @Override
    public IEventSource getEventSource() {
        return es;
    }

    @Override
    public Instrument getTodayInstrument(String instrumentId) {
        return instruments.get(instrumentId);
    }

    @Override
    public Collection<Instrument> getTodayInstruments() {
        return new HashSet<>(instruments.values());
    }

    @Override
    public ServiceRuntimeStatus getStatus() {
        return status;
    }

    @Override
    public TraderGatewayContext getTraderGatewayContext(int traderId) throws UnknownTraderIdException {
        return findContextByTraderId(traderId).getTraderGatewayContext();
    }

    @Override
    public Collection<TraderGatewayContext> getTraderGatewayContexts() {
        var r = new HashSet<TraderGatewayContext>(64);
        traders.values().forEach(c -> {
            r.add(c.getTraderGatewayContext());
        });
        return r;
    }

    @Override
    public void registerTrader(int traderId, ITraderGateway trader) throws DuplicatedTraderIdException {
        Objects.requireNonNull(trader);
        if (traders.containsKey(traderId)) {
            throw new DuplicatedTraderIdException(Integer.toString(traderId));
        }
        addTrader(traderId, trader);
    }

    @Override
    public void renew() throws TraderRenewException {
        changeStatus(TraderEngineStatuses.INITIALIZING);
        checkDataSourceAlgorithmNotNull();
        try {
            renewAccount();
        } catch (DataQueryException | UnexpectedErrorException ex) {
            changeStatus(TraderEngineStatuses.INIT_FAILED);
            throw new TraderRenewException(ex.getMessage(), ex);
        }
    }

    @Override
    public void request(Request request, Instrument instrument, Properties properties)
            throws IllegalRequestException, InvalidRequestException {
        Objects.requireNonNull(request);
        checkIllegalRequest(request, instrument);
        try {
            es0.publish(RequestDetail.class,
                        new RequestDetail(request, instrument, properties));
        } catch (NoSubscribedClassException ex) {
            /*
             * It shouldn't throw exception here unless the internal facilities
             * are not ready.
             */
            ex.printStackTrace();
        }
    }

    @Override
    public void settle() throws SettlementException {
        changeStatus(TraderEngineStatuses.SETTLING);
        checkDataSourceAlgorithmNotNull();
        try {
            settle(ds);
            settleAccount();
        } catch (TraderException ex) {
            throw new SettlementException(ex.getMessage(), ex);
        }
    }

    @Override
    public void unregisterTrader(int traderId) throws UnknownTraderIdException {
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
        var trCtx = new TraderContext(c);
        trader.setHandler(new TraderGatewayHandler(trCtx));
        traders.put(traderId, trCtx);
    }

    private ServiceRuntimeStatus buildStatus(TraderEngineStatuses enums) {
        return new ServiceRuntimeStatus(enums.code(), enums.message());
    }

    private void callOnException(TraderRuntimeException e) {
        /*
         * If something is wrong, tell user to handle it. If the handling is
         * wrong, tell user the handling is wrong.
         */
        publishEvent(TraderRuntimeException.class, e);
    }

    private void callOnStatusChange(ServiceRuntimeStatus s) {
        publishEvent(ServiceRuntimeStatus.class, s);
    }

    private boolean canClose(Contract c, Request request)
            throws InvalidRequestOffsetException, InvalidRequestDirectionException {
        if (c.getStatus() != ContractStatus.OPEN) {
            return false;
        }
        var offset = request.getOffset();
        var direction = request.getDirection();
        if (null == offset) {
            throw new InvalidRequestOffsetException("Offset null ptr.");
        }
        if (null == direction) {
            throw new InvalidRequestDirectionException("Direction null ptr.");
        }
        if (direction == Direction.BUY) {
            return c.getDirection() == Direction.SELL;
        } else {
            return c.getDirection() == Direction.BUY;
        }
    }

    private void changeStatus(TraderEngineStatuses enums) {
        this.status = buildStatus(enums);
        callOnStatusChange(this.status);
    }

    private Collection<Contract> checkAssetsClose(Request request, Instrument instrument)
            throws IllegalQuantityException, QuantityOverflowException, ContractNotFoundException,
                   InvalidRequestOffsetException, InvalidRequestDirectionException, DataAccessException,
                   MarginNotFoundException, NoTraderException {
        if (request.getQuantity() < 0) {
            throw new IllegalQuantityException("Illegal request quantity.");
        }
        var cs = getAvailableContracts(request);
        if (cs.size() < request.getQuantity()) {
            throw new QuantityOverflowException(request.getQuantity() + ">" + cs.size());
        }
        var r = new HashSet<Contract>(32);
        for (int i = 0; i < request.getQuantity(); ++i) {
            var ctr = cs.get(i);
            r.add(ctr);
            var c = algo.getCommission(request.getPrice(),
                                       instrument,
                                       request.getOffset(),
                                       ctr,
                                       request.getTradingDay());
            setFrozenClose(c,
                           ctr,
                           getMarginByContract(ctr));
        }
        return r;
    }

    private void checkAssetsOpen(Request request, Instrument instrument)
            throws IllegalQuantityException, DataAccessException, MoneyOverflowException,
                   AlgorithmException, NoTraderException {
        if (request.getQuantity() < 0) {
            throw new IllegalQuantityException("Illegal request quantity.");
        }
        var a = algo.getAmount(request.getPrice(), instrument);
        var m = algo.getMargin(request.getPrice(),
                               instrument);
        var c = algo.getCommission(request.getPrice(),
                                   instrument,
                                   request.getOffset(),
                                   null,
                                   request.getTradingDay());
        var total = request.getQuantity() * (m + c);
        var available = getAvailableMoney();
        if (available < total) {
            throw new MoneyOverflowException(total + ">" + available);
        }
        for (int i = 0; i < request.getQuantity(); ++i) {
            setFrozenOpen(a, m, c, request);
        }
    }

    private void checkDataSourceAlgorithmNotNull() {
        Objects.requireNonNull(ds);
        Objects.requireNonNull(algo);
    }

    private void checkIllegalRequest(Request request, Instrument instrument)
            throws IllegalRequestException, InvalidRequestException {
        try {
            Objects.requireNonNull(request.getInstrumentId(), "Instrument ID null.");
            Objects.requireNonNull(request.getAction(), "Action null.");
            Objects.requireNonNull(request.getDirection(), "Direction null.");
            Objects.requireNonNull(request.getExchangeId(), "Exchange ID null.");
            Objects.requireNonNull(request.getOffset(), "Offset null.");
            Objects.requireNonNull(request.getPrice(), "Price null.");
            Objects.requireNonNull(request.getOrderId(), "Order ID null.");
            Objects.requireNonNull(request.getQuantity(), "Quantity null.");
            Objects.requireNonNull(request.getTraderId(), "Trader ID null.");
        } catch (NullPointerException ex) {
            throw new IllegalRequestException(ex.getMessage(), ex);
        }
        if (!request.getInstrumentId().equals(instrument.getInstrumentId())) {
            throw new InvalidRequestException("Instrument information has a different instrument ID: "
                                              + instrument.getInstrumentId() + ".");
        }
    }

    private void clearCommissions(ITraderDataConnection conn) throws DataAccessException {
        try {
            for (var c : conn.getCommissions()) {
                conn.removeCommission(c.getCommissionId());
            }
        } catch (DataRemovalException | DataQueryException ex) {
            throw new DataAccessException(ex.getMessage(), ex);
        }
    }

    private void clearContracts(Collection<Contract> cs, ITraderDataConnection conn)
            throws DataAccessException {
        Objects.requireNonNull(cs);
        try {
            for (var c : cs) {
                conn.removeContract(c.getContractId());
            }
        } catch (DataRemovalException ex) {
            throw new DataAccessException(ex.getMessage(), ex);
        }
    }

    private void clearInternals() {
        orderTraders.clear();
        instruments.clear();
        traders.values().forEach(s -> {
            s.clear();
        });
    }

    private void clearMargins(Collection<Margin> margins, ITraderDataConnection conn) throws DataAccessException {
        try {
            for (var m : margins) {
                conn.removeMargin(m.getMarginId());
            }
        } catch (DataRemovalException ex) {
            throw new DataAccessException(ex.getMessage(),
                                          ex);
        }
    }

    private void clearWithdrawDeposit(Collection<Withdraw> ws,
                                      Collection<Deposit> ds,
                                      ITraderDataConnection conn) throws DataAccessException {
        Objects.requireNonNull(ws);
        Objects.requireNonNull(ds);
        try {
            for (var w : ws) {
                conn.removeMargin(w.getWithdrawId());
            }
            for (var d : ds) {
                conn.removeDeposit(d.getDepositId());
            }
        } catch (DataRemovalException ex) {
            throw new DataAccessException(ex.getMessage(),
                                          ex);
        }
    }

    private void decideTrader(Request request)
            throws TraderDisabledException, UnknownTraderIdException, NoTraderException {
        var rt = getProperTrader(request);
        if (!Objects.equals(rt.getTraderId(), request.getTraderId())) {
            request.setTraderId(rt.getTraderId());
        }
    }

    private void deleteOrderRequest(Request request)
            throws UnknownTraderIdException, UnknownOrderIdException {
        var orderId = request.getOrderId();
        var traderId = findTraderIdByOrderId(orderId);
        var ctx = findContextByTraderId(traderId);
        var h = ctx.getHandler();
        Objects.requireNonNull(h);
        /*
         * Mock DELETE response.
         */
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
        } catch (Throwable th) {
            callOnException(new TraderRuntimeException(th.getMessage(), th));
        }
    }

    private void deleteRequest(Request request, TraderContext context)
            throws DestinatedIdNotFoundException, CountDownNotFoundException {
        var ids = context.getDestinatedIds(request.getOrderId());
        if (ids == null) {
            throw new DestinatedIdNotFoundException("Source ID: " + request.getOrderId() + ".");
        }
        for (var i : ids) {
            /*
             * If the order is fulfilled, don't cancel it any more.
             */
            var cd = context.getDownCountByDestId(i);
            if (cd == null) {
                throw new CountDownNotFoundException("Destinated ID: " + i + ").");
            }
            if (cd <= 0) {
                continue;
            }
            var c = Utils.copy(request);
            c.setOrderId(i);
            context.insert(c);
        }
    }

    private void dispatchRequest(RequestDetail detail) {
        try (var conn = ds.getConnection()) {
            if (null == detail.getRequest().getAction()) {
                throw new IllegalRequestActionException("Action null ptr.");
            }
            var ctx = findContextByTraderId(detail.getRequest().getTraderId());
            var tradingDay = ctx.getGatewayInfo().getTradingDay();
            detail.getRequest().setTradingDay(tradingDay);
            switch (detail.getRequest().getAction()) {
                case ActionType.DELETE:
                    forDelete(detail.getRequest());
                    break;
                case ActionType.NEW:
                    forNew(detail.getRequest(), detail.getInstrument());
                    break;
                default:
                    throw new IllegalRequestActionException(
                            "Illegal request action: " + detail.getRequest().getAction() + ".");
            }
            conn.addRequest(detail.getRequest());
        } catch (Exception ex) {
            Loggers.getLogger(TraderEngine.class.getCanonicalName())
                   .log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private TraderContext findContextByTraderId(int traderId) throws UnknownTraderIdException {
        if (!traders.containsKey(traderId)) {
            throw new UnknownTraderIdException(Integer.toString(traderId));
        }
        return traders.get(traderId);
    }

    private TraderContext findContextRandomly(Request request) throws NoTraderException {
        var a = new ArrayList<>(traders.keySet());
        traders.forEach((k, v) -> {
            if (v.isEnabled()) {
                a.add(k);
            }
        });
        if (a.isEmpty()) {
            throw new NoTraderException("No existing trader.");
        }
        var traderId = a.get(new Random().nextInt(a.size()));
        orderTraders.put(request.getOrderId(), traderId);
        return traders.get(traderId);
    }

    private Map<String, Instrument> findRelatedInstruments(
            Collection<String> instrumentIds, ITraderDataConnection conn) throws InstrumentNotFoundException {
        final var r = new HashMap<String, Instrument>(512);
        try {
            for (var i : instrumentIds) {
                var instrument = conn.getInstrumentById(i);
                if (instrument == null) {
                    throw new InstrumentNotFoundException(i);
                }
                r.put(i, instrument);
            }
            return r;
        } catch (DataQueryException ex) {
            throw new InstrumentNotFoundException("Fail acquiring instrument information.");
        }
    }

    private Map<String, SettlementPrice> findSettlementPrices(
            Collection<String> instrumentIds, ITraderDataConnection conn)
            throws SettlementNotFoundException, DataAccessException {
        final var r = new HashMap<String, SettlementPrice>(512);
        try {
            for (var i : instrumentIds) {
                var price = conn.getSettlementPriceByInstrumentId(i);
                if (price == null) {
                    throw new SettlementNotFoundException(i);
                }
                r.put(i, price);
            }
            return r;
        } catch (DataQueryException ex) {
            throw new DataAccessException(ex.getMessage(),
                                          ex);
        }
    }

    private Integer findTraderIdByOrderId(long orderId) throws UnknownOrderIdException {
        var traderId = orderTraders.get(orderId);
        if (traderId == null) {
            throw new UnknownOrderIdException(Long.toString(orderId));
        }
        return traderId;
    }

    private void forDelete(Request request)
            throws UnknownTraderIdException, CountDownNotFoundException, DestinatedIdNotFoundException {
        Objects.requireNonNull(request);
        synchronized (this) {
            forwardDeleteRequest(request, request.getTraderId());
        }
    }

    private void forNew(Request request, Instrument instrument)
            throws UnknownTraderIdException, IllegalQuantityException, DataAccessException,
                   MoneyOverflowException, TraderDisabledException, NoTraderException,
                   QuantityOverflowException, ContractNotFoundException, InvalidRequestOffsetException,
                   InvalidRequestDirectionException, DeepCopyException, AlgorithmException,
                   MarginNotFoundException {
        checkDataSourceAlgorithmNotNull();
        Objects.requireNonNull(instrument);
        /*
         * Remmeber the instrument it once operated.
         */
        instruments.put(instrument.getInstrumentId(), instrument);
        synchronized (this) {
            if (request.getOffset() == Offset.OPEN) {
                decideTrader(request);
                checkAssetsOpen(request, instrument);
                forwardNewRequest(request, request.getTraderId());
            } else {
                var cs = checkAssetsClose(request, instrument);
                var grp = group(cs, request);
                for (var r : grp) {
                    forwardNewRequest(r, r.getTraderId());
                }
            }
        }
    }

    private void forwardDeleteRequest(Request request, Integer traderId)
            throws UnknownTraderIdException, DestinatedIdNotFoundException,
                   CountDownNotFoundException {
        var ctx = findContextByTraderId(traderId);
        deleteRequest(request, ctx);
    }

    private void forwardNewRequest(Request request, Integer traderId) throws UnknownTraderIdException {
        var ctx = findContextByTraderId(traderId);
        newRequest(request, ctx);
    }

    private List<Contract> getAvailableContracts(Request request)
            throws ContractNotFoundException, InvalidRequestOffsetException,
                   InvalidRequestDirectionException, DataAccessException {
        try (var conn = ds.getConnection()) {
            final var cs = conn.getContractsByInstrumentId(request.getInstrumentId());
            if (cs == null) {
                throw new ContractNotFoundException(request.getInstrumentId());
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
        } catch (SQLException | ClassNotFoundException | DataQueryException ex) {
            throw new DataAccessException(ex.getMessage(), ex);
        }
    }

    private double getAvailableMoney() throws DataAccessException, AlgorithmException {
        var a = getSettledAccount();
        return (a.getBalance() - a.getMargin() - a.getFrozenMargin() - a.getFrozenCommission());
    }

    private Collection<Contract> getContractsByTrades(Collection<Trade> rsps)
            throws ContractNotFoundException, DataAccessException {
        final var cs = new HashSet<Contract>(128);
        try (var conn = ds.getConnection()) {
            for (var r : rsps) {
                var s = conn.getContractsByTradeId(r.getTradeId());
                if (s == null) {
                    throw new ContractNotFoundException("Trade ID:" + r.getTradeId() + ").");
                }
                cs.addAll(s);
            }
            return cs;
        } catch (DataQueryException | SQLException | ClassNotFoundException ex) {
            throw new DataAccessException(ex.getMessage(), ex);
        }
    }

    private Margin getMarginByContract(Contract contract)
            throws DataAccessException, MarginNotFoundException {
        Margin m = null;
        try (ITraderDataConnection conn = ds.getConnection()) {
            var trade = conn.getTradeById(contract.getTradeId());
            var margins = conn.getMarginsByOrderId(trade.getOrderId());
            for (var x : margins) {
                if (x.getContractId().equals(contract.getContractId())) {
                    m = x;
                    break;
                }
            }
            if (m == null) {
                throw new MarginNotFoundException("Margin not found for contract ID: " + contract.getContractId() + ".");
            }
            return m;
        } catch (DataQueryException | ClassNotFoundException | SQLException ex) {
            throw new DataAccessException(ex.getMessage(), ex);
        }
    }

    private TraderContext getProperTrader(Request request)
            throws TraderDisabledException, UnknownTraderIdException, NoTraderException {
        var traderId = request.getTraderId();
        if (traderId == null) {
            return findContextRandomly(request);
        } else {
            var ctx = findContextByTraderId(traderId);
            if (!ctx.isEnabled()) {
                throw new TraderDisabledException(Long.toString(traderId));
            }
            orderTraders.put(request.getOrderId(), traderId);
            return ctx;
        }
    }

    private Collection<String> getRalatedInstrumentIds() {
        return null;
    }

    private TraderContext findAnyContext() throws NoTraderException {
        if (traders.isEmpty()) {
            throw new NoTraderException("No available trader.");
        }
        return traders.values().iterator().next();
    }

    private Account getSettledAccount() throws DataAccessException, AlgorithmException {
        try (var conn = ds.getConnection()) {
            final var tradingDay = findAnyContext().getGatewayInfo().getTradingDay();
            final var ids = getRalatedInstrumentIds();
            final var settlePrices = findSettlementPrices(ids, conn);
            final var relatedInstruments = findRelatedInstruments(ids, conn);
            final var positions = algo.getPositions(conn.getContracts(),
                                                    conn.getCommissions(),
                                                    conn.getMargins(),
                                                    settlePrices,
                                                    relatedInstruments,
                                                    tradingDay);
            return algo.getAccount(conn.getAccount(),
                                   conn.getDeposits(),
                                   conn.getWithdraws(),
                                   positions);
        } catch (DataQueryException | SQLException | ClassNotFoundException ex) {
            throw new DataAccessException(ex.getMessage(),
                                          ex);
        } catch (TraderException ex) {
            throw new AlgorithmException(ex.getMessage(),
                                         ex);
        }
    }

    private void settle(ITraderDataSource ds)
            throws DataAccessException, ContractNotFoundException, AlgorithmException,
                   NoTraderException, UnknownTraderIdException, UnknownOrderIdException,
                   InvalidContractException, QuantityOverflowException, InstrumentNotFoundException,
                   WrongOrderIdException {
        try (var conn = ds.getConnection()) {
            var rs = conn.getRequests();
            var tradingDay = findAnyContext().getGatewayInfo().getTradingDay();
            Objects.requireNonNull(rs);
            for (var r : rs) {
                if (!r.getTradingDay().equals(tradingDay)) {
                    continue;
                }
                settleRequest(r, conn);
            }
            // Clear everyday to avoid mem leak.
            clearInternals();
        } catch (DataQueryException | ClassNotFoundException | SQLException ex) {
            throw new DataAccessException(ex.getMessage(), ex);
        }
    }

    private Collection<Request> group(Collection<Contract> cs, Request request)
            throws DeepCopyException, NoTraderException {
        final var today = new HashMap<Integer, Request>(64);
        final var yd = new HashMap<Integer, Request>(64);
        var tradingDay = findAnyContext().getGatewayInfo().getTradingDay();
        for (var c : cs) {
            if (c.getOpenTradingDay().isBefore(tradingDay)) {
                Request o = yd.get(c.getTraderId());
                if (o == null) {
                    o = Utils.copy(request);
                    if (o == null) {
                        throw new DeepCopyException(Request.class.getCanonicalName());
                    }
                    o.setOffset(Offset.CLOSE_YD);
                    o.setQuantity(0L);
                    o.setTraderId(c.getTraderId());
                }
                o.setQuantity(o.getQuantity() + 1);
            } else {
                Request o = today.get(c.getTraderId());
                if (o == null) {
                    o = Utils.copy(request);
                    if (o == null) {
                        throw new DeepCopyException(Request.class.getCanonicalName());
                    }
                    o.setOffset(Offset.CLOSE_TODAY);
                    o.setQuantity(0L);
                    o.setTraderId(c.getTraderId());
                }
                o.setQuantity(o.getQuantity() + 1);
            }
        }

        var r = new HashSet<Request>(today.values());
        r.addAll(yd.values());
        return r;
    }

    private void initAccount(Account a) throws DataAccessException, NoTraderException {
        Objects.requireNonNull(a);
        try (var conn = ds.getConnection()) {
            final var tradingDay = findAnyContext().getGatewayInfo().getTradingDay();
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
        } catch (SQLException | ClassNotFoundException | DataUpdateException ex) {
            throw new DataAccessException(ex.getMessage(), ex);
        }
    }

    private void newRequest(Request request, TraderContext context) {
        Long destId = context.getDestinatedId(request.getOrderId(), request.getQuantity());
        request.setOrderId(destId);
        context.insert(request);
    }

    private void renewAccount() throws UnexpectedErrorException, DataQueryException {
        ITraderDataConnection conn = null;
        try {
            conn = ds.getConnection();
            conn.transaction();
            initAccount(conn.getAccount());
            clearContracts(conn.getContractsByStatus(ContractStatus.CLOSED),
                           conn);
            clearMargins(conn.getMarginsByStatus(FeeStatus.REMOVED),
                         conn);
            clearCommissions(conn);
            clearWithdrawDeposit(conn.getWithdraws(),
                                 conn.getDeposits(),
                                 conn);
            conn.commit();
            changeStatus(TraderEngineStatuses.WORKING);
        } catch (DataQueryException ex) {
            rollback(conn);
            throw ex;
        } catch (Throwable th) {
            rollback(conn);
            throw new UnexpectedErrorException(th.getMessage(), th);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void rollback(ITraderDataConnection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ex) {
            callOnException(new TraderRuntimeException(ex.getMessage(),
                                                       ex));
        }
    }

    private void setFrozenClose(double commission, Contract contract, Margin margin)
            throws DataAccessException, NoTraderException {
        ITraderDataConnection conn = null;
        try {
            conn = ds.getConnection();
            final var tradingDay = findAnyContext().getGatewayInfo().getTradingDay();
            conn.transaction();
            /*
             * Update contracts status to make it frozen.
             */
            contract.setStatus(ContractStatus.CLOSING);
            conn.updateContract(contract);
            /*
             * Update margin status to make it frozen.
             */
            margin.setStatus(FeeStatus.FORZEN);
            conn.updateMargin(margin);
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
        } catch (ClassNotFoundException | SQLException | DataException ex) {
            rollback(conn);
            throw new DataAccessException(ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void setFrozenOpen(double amount, double margin, double commission, Request request)
            throws DataAccessException, NoTraderException {
        ITraderDataConnection conn = null;
        try {
            conn = ds.getConnection();
            final var tradingDay = findAnyContext().getGatewayInfo().getTradingDay();
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
            ctr.setTag(request.getTag());
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
            cmn.setTag(request.getTag());
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
            mn.setTag(request.getTag());
            conn.addMargin(mn);
            /*
             * Commit change.
             */
            conn.commit();
        } catch (ClassNotFoundException | SQLException | DataException ex) {
            rollback(conn);
            throw new DataAccessException(ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void settleAccount() throws DataAccessException, UnexpectedErrorException {
        ITraderDataConnection conn = null;
        try {
            /*
             * Update settlement into db.
             */
            conn = ds.getConnection();
            conn.transaction();
            conn.updateAccount(getSettledAccount());
            conn.commit();
            changeStatus(TraderEngineStatuses.WORKING);
        } catch (DataUpdateException ex) {
            rollback(conn);
            throw new DataAccessException(ex.getMessage(),
                                          ex);
        } catch (Throwable th) {
            rollback(conn);
            throw new UnexpectedErrorException(th.getMessage(),
                                               th);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void settleRequest(Request r, ITraderDataConnection conn)
            throws DataQueryException, ContractNotFoundException, DataAccessException,
                   QuantityOverflowException, InvalidContractException, UnknownTraderIdException,
                   UnknownOrderIdException, WrongOrderIdException, InstrumentNotFoundException {
        var orderId = r.getOrderId();
        Objects.requireNonNull(orderId);
        var trades = conn.getTradesByOrderId(orderId);
        Objects.requireNonNull(trades);
        var ctrs = getContractsByTrades(trades);
        Objects.requireNonNull(ctrs);
        var cals = conn.getResponseByOrderId(orderId);
        Objects.requireNonNull(cals);
        Order o = algo.getOrder(r,
                                ctrs,
                                trades,
                                cals,
                                instruments);
        var s = o.getStatus();
        if (s == OrderStatus.ACCEPTED
            || s == OrderStatus.QUEUED
            || s == OrderStatus.UNQUEUED) {
            deleteOrderRequest(r);
        }
    }

    <T> void publishEvent(Class<T> clazz, T object) {
        if (es == null || es.isEmpty()) {
            return;
        }
        try {
            es.publish(clazz, object);
        } catch (NoSubscribedClassException ex) {
            /*
             * It shouldn't throw exception here unless the internal facilities
             * are not ready.
             */
            ex.printStackTrace();
        }
    }
}
