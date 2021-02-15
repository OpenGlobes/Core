package com.openglobes.core.trader;

import com.openglobes.core.utils.Utils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AlgorithmData {
    private final Collection<Deposit> deposits = new HashSet<>();
    private final Collection<Withdraw> withdraws = new HashSet<>();
    private final Map<String, Instrument> instruments = new HashMap<>();
    private final Collection<Contract> contracts = new HashSet<>();
    private final Collection<Trade> trades = new HashSet<>();
    private final Collection<Response> responses = new HashSet<>();
    private final ITraderEngineAlgorithm algorithm = new DefaultTraderEngineAlgorithm();
    private final Map<Long, Request> requests = new HashMap<>();

    protected AlgorithmData() {
        setDeposits();
        setWithdraws();
        setInstruments();
        setRequests();
        setTrades();
        setContracts();
        setResponses();
    }

    protected Collection<Trade> getTradesByOrderId(Long orderId) {
        var r = new HashSet<Trade>();
        trades.forEach(x -> {
            if (x.getOrderId().equals(orderId)) {
                r.add(x);
            }
        });
        return r;
    }

    protected Collection<Response> getResponsesByOrderId(Long orderId) {
        var r = new HashSet<Response>();
        responses.forEach(rs -> {
            if (rs.getOrderId().equals(orderId)) {
                r.add(rs);
            }
        });
        return r;
    }

    protected Collection<Contract> getContractsByTrades(Collection<Trade> trades) {
        var r = new HashSet<Contract>();
        trades.forEach(tr -> {
            for (var c : contracts) {
                if (c.getTradeId().equals(tr.getTradeId())) {
                    r.add(c);
                }
            }
        });
        return r;
    }

    private void setResponses() {
        var r = new Response();

        // Order ID = 1L
        r.setAction(ActionType.NEW);
        r.setDirection(Direction.BUY);
        r.setOffset(Offset.OPEN);
        r.setSignature(Utils.nextUuid().toString());
        r.setStatus(OrderStatus.ACCEPTED);
        r.setInstrumentId("c2105");
        r.setOrderId(1L);
        r.setResponseId(1L);
        r.setStatusMessage("ACCEPTED");
        r.setTimestamp(ZonedDateTime.now().minusMinutes(11).minusDays(1));
        r.setTraderId(1);
        r.setTradingDay(LocalDate.now().minusDays(1));
        responses.add(r);

        r = Utils.copy(r);
        r.setStatus(OrderStatus.ALL_TRADED);
        r.setStatusMessage("ALL_TRADED");
        r.setResponseId(2L);
        r.setTimestamp(r.getTimestamp().plusSeconds(1));
        responses.add(r);

        // Order ID = 2L
        r = new Response();

        r.setAction(ActionType.NEW);
        r.setDirection(Direction.SELL);
        r.setOffset(Offset.OPEN);
        r.setSignature(Utils.nextUuid().toString());
        r.setStatus(OrderStatus.ACCEPTED);
        r.setInstrumentId("c2105");
        r.setOrderId(2L);
        r.setResponseId(3L);
        r.setStatusMessage("ACCEPTED");
        r.setTimestamp(ZonedDateTime.now().minusMinutes(7).minusDays(1));
        r.setTraderId(2);
        r.setTradingDay(LocalDate.now().minusDays(1));
        responses.add(r);

        r = Utils.copy(r);
        r.setStatus(OrderStatus.ALL_TRADED);
        r.setStatusMessage("ALL_TRADED");
        r.setResponseId(4L);
        r.setTimestamp(r.getTimestamp().plusSeconds(1));
        responses.add(r);

        // Order ID = 3L
        r = new Response();

        r.setAction(ActionType.NEW);
        r.setDirection(Direction.SELL);
        r.setOffset(Offset.OPEN);
        r.setSignature(Utils.nextUuid().toString());
        r.setStatus(OrderStatus.ACCEPTED);
        r.setInstrumentId("c2105");
        r.setOrderId(3L);
        r.setResponseId(5L);
        r.setStatusMessage("ACCEPTED");
        r.setTimestamp(ZonedDateTime.now().minusMinutes(7));
        r.setTraderId(3);
        r.setTradingDay(LocalDate.now());
        responses.add(r);

        r = Utils.copy(r);
        r.setStatus(OrderStatus.ALL_TRADED);
        r.setStatusMessage("ALL_TRADED");
        r.setResponseId(6L);
        r.setTimestamp(r.getTimestamp().plusSeconds(1));
        responses.add(r);

        // Order ID = 4L
        r = new Response();

        r.setAction(ActionType.NEW);
        r.setDirection(Direction.BUY);
        r.setOffset(Offset.CLOSE);
        r.setSignature(Utils.nextUuid().toString());
        r.setStatus(OrderStatus.ACCEPTED);
        r.setInstrumentId("c2105");
        r.setOrderId(4L);
        r.setResponseId(7L);
        r.setStatusMessage("ACCEPTED");
        r.setTimestamp(ZonedDateTime.now().minusMinutes(3));
        r.setTraderId(2);
        r.setTradingDay(LocalDate.now());
        responses.add(r);

        r = Utils.copy(r);

        r.setTraderId(3);
        r.setResponseId(8L);
        responses.add(r);
    }

    private void setTrades() {
        Trade t = new Trade();

        // Order ID = 1L
        t.setAction(ActionType.NEW);
        t.setDirection(Direction.BUY);
        t.setOffset(Offset.OPEN);
        t.setInstrumentId("c2105");
        t.setOrderId(1L);
        t.setTraderId(1);
        t.setTimestamp(ZonedDateTime.now().minusMinutes(11).minusDays(1));
        t.setTradingDay(LocalDate.now().minusDays(1));
        t.setTradeId(1L);
        t.setPrice(2965.0);
        t.setQuantity(2L);
        t.setSignature(Utils.nextUuid().toString());
        trades.add(t);

        // Order ID = 2L
        t = Utils.copy(t);
        t.setAction(ActionType.NEW);
        t.setDirection(Direction.SELL);
        t.setOffset(Offset.OPEN);
        t.setInstrumentId("c2105");
        t.setOrderId(2L);
        t.setTraderId(2);
        t.setTimestamp(ZonedDateTime.now().minusMinutes(7).minusDays(1));
        t.setTradingDay(LocalDate.now().minusDays(1));
        t.setTradeId(2L);
        t.setPrice(2955.0);
        t.setQuantity(1L);
        t.setSignature(Utils.nextUuid().toString());
        trades.add(t);

        // Order ID = 3L
        t = Utils.copy(t);
        t.setAction(ActionType.NEW);
        t.setDirection(Direction.SELL);
        t.setOffset(Offset.OPEN);
        t.setInstrumentId("c2105");
        t.setOrderId(3L);
        t.setTraderId(3);
        t.setTimestamp(ZonedDateTime.now().minusMinutes(7));
        t.setTradingDay(LocalDate.now());
        t.setTradeId(3L);
        t.setPrice(2955.0);
        t.setQuantity(2L);
        t.setSignature(Utils.nextUuid().toString());
        trades.add(t);
    }

    private void setContracts() {
        Contract c = new Contract();
        // Order ID = 1L
        c.setInstrumentId("c2105");
        c.setDirection(Direction.BUY);
        c.setTimestamp(ZonedDateTime.now().minusMinutes(10).minusDays(1));
        c.setTag("buy-open-request");
        c.setContractId(1L);
        c.setCloseAmount(null);
        c.setCloseTradingDay(null);
        c.setOpenAmount(29650.0);
        c.setOpenTimestamp(ZonedDateTime.now().minusMinutes(11).minusDays(1));
        c.setOpenTradingDay(LocalDate.now().minusDays(1));
        c.setStatus(ContractStatus.OPEN);
        c.setTradeId(1L);
        c.setTraderId(1);
        contracts.add(c);

        c = Utils.copy(c);
        c.setContractId(2L);
        contracts.add(c);

        // Order ID = 2L
        c = new Contract();
        c.setInstrumentId("c2105");
        c.setDirection(Direction.SELL);
        c.setTimestamp(ZonedDateTime.now().minusMinutes(6).minusDays(1));
        c.setTag("sell-open-request");
        c.setContractId(3L);
        c.setCloseAmount(null);
        c.setCloseTradingDay(null);
        c.setOpenAmount(29550.0);
        c.setOpenTimestamp(ZonedDateTime.now().minusMinutes(7).minusDays(1));
        c.setOpenTradingDay(LocalDate.now().minusDays(1));
        c.setStatus(ContractStatus.CLOSING);
        c.setTradeId(2L);
        c.setTraderId(2);
        contracts.add(c);

        // Order ID = 3L
        c = new Contract();
        c.setInstrumentId("c2105");
        c.setDirection(Direction.SELL);
        c.setTimestamp(ZonedDateTime.now().minusMinutes(6));
        c.setTag("sell-open-request");
        c.setContractId(4L);
        c.setCloseAmount(null);
        c.setCloseTradingDay(null);
        c.setOpenAmount(29550.0);
        c.setOpenTimestamp(ZonedDateTime.now().minusMinutes(7));
        c.setOpenTradingDay(LocalDate.now());
        c.setStatus(ContractStatus.CLOSING);
        c.setTradeId(3L);
        c.setTraderId(3);
        contracts.add(c);

        c = Utils.copy(c);
        c.setContractId(5L);
        c.setStatus(ContractStatus.OPEN);
        contracts.add(c);
    }

    private void setRequests() {
        Request r = new Request();

        r.setTradingDay(LocalDate.now().minusDays(1));
        r.setRequestId(1L);
        r.setInstrumentId("c2105");
        r.setExchangeId("DCE");
        r.setAction(ActionType.NEW);
        r.setDirection(Direction.BUY);
        r.setOffset(Offset.OPEN);
        r.setOrderId(1L);
        r.setPrice(2965.0);
        r.setQuantity(2L);
        r.setSignature(Utils.nextUuid().toString());
        r.setTag("buy-open-request");
        r.setTraderId(1);
        r.setUpdateTimestamp(ZonedDateTime.now().minusMinutes(11).minusDays(1));
        requests.put(r.getRequestId(),
                     r);

        r = new Request();

        r.setUpdateTimestamp(ZonedDateTime.now().minusMinutes(7).minusDays(1));
        r.setTag("sell-open-request");
        r.setSignature(Utils.nextUuid().toString());
        r.setQuantity(1L);
        r.setPrice(2955.0);
        r.setTraderId(2);
        r.setDirection(Direction.SELL);
        r.setOrderId(2L);
        r.setOffset(Offset.OPEN);
        r.setAction(ActionType.NEW);
        r.setExchangeId("DCE");
        r.setInstrumentId("c2105");
        r.setRequestId(2L);
        r.setTradingDay(LocalDate.now().minusDays(1));
        requests.put(r.getRequestId(),
                     r);

        r = new Request();

        r.setUpdateTimestamp(ZonedDateTime.now().minusMinutes(11));
        r.setTag("sell-open-request");
        r.setSignature(Utils.nextUuid().toString());
        r.setQuantity(2L);
        r.setPrice(2955.0);
        r.setTraderId(3);
        r.setDirection(Direction.SELL);
        r.setOrderId(3L);
        r.setOffset(Offset.OPEN);
        r.setAction(ActionType.NEW);
        r.setExchangeId("DCE");
        r.setInstrumentId("c2105");
        r.setRequestId(3L);
        r.setTradingDay(LocalDate.now());
        requests.put(r.getRequestId(),
                     r);

        r = new Request();

        r.setUpdateTimestamp(ZonedDateTime.now().minusMinutes(3));
        r.setTag("buy-close-request");
        r.setSignature(Utils.nextUuid().toString());
        r.setQuantity(2L);
        r.setPrice(2945.0);
        r.setTraderId(null);
        r.setDirection(Direction.BUY);
        r.setOrderId(4L);
        r.setOffset(Offset.CLOSE);
        r.setAction(ActionType.NEW);
        r.setExchangeId("DCE");
        r.setInstrumentId("c2105");
        r.setRequestId(4L);
        r.setTradingDay(LocalDate.now());
        requests.put(r.getRequestId(),
                     r);
    }

    private void setWithdraws() {
        Withdraw w = new Withdraw();
        w.setTradingDay(LocalDate.now());

        w.setAmount(1000.0);
        w.setTimestamp(ZonedDateTime.now());
        w.setWithdrawId(1L);
        withdraws.add(w);

        w = Utils.copy(w);
        w.setAmount(500.0);
        w.setTimestamp(ZonedDateTime.now());
        w.setWithdrawId(2L);
        withdraws.add(w);

        w = Utils.copy(w);
        w.setAmount(600.0);
        w.setTimestamp(ZonedDateTime.now());
        w.setWithdrawId(3L);
        withdraws.add(w);
    }

    private void setDeposits() {
        Deposit d = new Deposit();
        d.setTradingDay(LocalDate.now());

        d.setAmount(4000.0);
        d.setTimestamp(ZonedDateTime.now());
        d.setDepositId(1L);
        deposits.add(d);

        d = Utils.copy(d);
        d.setAmount(2500.0);
        d.setTimestamp(ZonedDateTime.now());
        d.setDepositId(2L);
        deposits.add(d);
    }

    protected Map<Long, Request> requests() {
        return requests;
    }

    protected Collection<Deposit> deposits() {
        return deposits;
    }

    protected Collection<Withdraw> withdraws() {
        return withdraws;
    }

    protected Instrument instrument(String instrumentId) {
        return instruments.get(instrumentId);
    }

    protected ITraderEngineAlgorithm algorithm() {
        return algorithm;
    }

    private void setInstruments() {
        Instrument i = new Instrument();

        i.setCommissionCloseRatio(0.0);
        i.setCommissionCloseTodayRatio(1.2);
        i.setInstrumentId("c2105");
        i.setTimestamp(ZonedDateTime.now());
        i.setEndDate(LocalDate.of(2021,
                                  5,
                                  15));
        i.setCommissionType(RatioType.BY_VOLUMN);
        i.setCommissionOpenRatio(1.2);
        i.setExchangeId("DCE");
        i.setMarginRatio(.09);
        i.setMarginType(RatioType.BY_MONEY);
        i.setMultiple(10L);
        i.setPriceTick(1.0);
        i.setStartDate(LocalDate.of(2020,
                                    5,
                                    1));
        instruments.put("c2105",
                        i);

        i = Utils.copy(i);
        i.setInstrumentId("c2109");
        i.setEndDate(LocalDate.of(2021,
                                  9,
                                  15));
        i.setStartDate(LocalDate.of(2020,
                                    9,
                                    1));
        instruments.put("c2109",
                        i);

        i = Utils.copy(i);
        i.setInstrumentId("x2109");
        i.setCommissionOpenRatio(.09);
        i.setCommissionCloseRatio(0.0);
        i.setCommissionCloseTodayRatio(.09);
        i.setCommissionType(RatioType.BY_MONEY);
        i.setMarginRatio(1234.0);
        i.setMarginType(RatioType.BY_VOLUMN);
        i.setEndDate(LocalDate.of(2021,
                                  9,
                                  15));
        i.setStartDate(LocalDate.of(2020,
                                    9,
                                    1));
        instruments.put("x2109",
                        i);
    }
}
