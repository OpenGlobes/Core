/*
 * Copyright (C) 2021 Hongbao Chen <chenhongbao@outlook.com>
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

import com.openglobes.core.utils.Utils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AlgorithmData {

    private final Account account = new Account();
    private final ITraderEngineAlgorithm algorithm = new DefaultTraderEngineAlgorithm();
    private final Collection<Commission> commissions = new HashSet<>(8);
    private final Collection<Contract> contracts = new HashSet<>(8);
    private final Collection<Deposit> deposits = new HashSet<>(8);
    private final Map<String, Instrument> instruments = new HashMap<>(8);
    private final Collection<Margin> margins = new HashSet<>(8);
    private final Map<Long, Request> requests = new HashMap<>(8);
    private final Collection<Response> responses = new HashSet<>(8);
    private final Map<String, SettlementPrice> settlements = new HashMap<>(8);
    private final Collection<Trade> trades = new HashSet<>(8);
    private final Collection<Withdraw> withdraws = new HashSet<>(8);

    protected AlgorithmData() {
        setDeposits();
        setWithdraws();
        setInstruments();
        setRequests();
        setTrades();
        setContracts();
        setResponses();
        setCommissions();
        setMargins();
        setSettlements();
        setAccount();
    }

    protected Account account() {
        return account;
    }

    protected ITraderEngineAlgorithm algorithm() {
        return algorithm;
    }

    protected Collection<Commission> commissions() {
        return commissions;
    }

    protected Collection<Contract> contracts() {
        return contracts;
    }

    protected Collection<Deposit> deposits() {
        return deposits;
    }

    protected Contract getContractById(Long contractId) {
        for (var c : contracts) {
            if (contractId.equals(c.getContractId())) {
                return c;
            }
        }
        throw new NullPointerException("Contract not found: " + contractId + ".");
    }

    protected Collection<Contract> getContractsByTrades(Collection<Trade> trades) {
        var r = new HashSet<Contract>(8);
        trades.forEach(tr -> {
            contracts.stream()
                     .filter(c -> (c.getTradeId().equals(tr.getTradeId())))
                     .forEachOrdered(c -> r.add(c));
        });
        return r;
    }

    protected Collection<Response> getResponsesByOrderId(Long orderId) {
        var r = new HashSet<Response>(8);
        responses.stream()
                 .filter(rs -> rs.getOrderId().equals(orderId))
                 .forEach(rs -> r.add(rs));
        return r;
    }

    protected Collection<Trade> getTradesByOrderId(Long orderId) {
        var r = new HashSet<Trade>(8);
        trades.stream()
              .filter(x -> x.getOrderId().equals(orderId))
              .forEach(x -> r.add(x));
        return r;
    }

    protected Instrument instrument(String instrumentId) {
        return instruments.get(instrumentId);
    }

    protected Map<String, Instrument> instruments() {
        return instruments;
    }

    protected Collection<Margin> margins() {
        return margins;
    }

    protected Map<Long, Request> requests() {
        return requests;
    }

    protected Map<String, SettlementPrice> settlements() {
        return settlements;
    }

    protected Collection<Withdraw> withdraws() {
        return withdraws;
    }

    private void setAccount() {
        account.setAccountId(1L);
        account.setPreBalance(1000.0D);
        account.setPreDeposit(1000.0D);
        account.setPreWithdraw(500.0D);
        account.setPreMargin(0.0D);
        account.setBalance(account.getPreBalance());
        account.setMargin(account.getPreMargin());
        account.setCloseProfit(0.0D);
        account.setCommission(0.0D);
        account.setDeposit(0.0D);
        account.setFrozenCommission(0.0D);
        account.setFrozenMargin(0.0D);
        account.setPositionProfit(150.0D);
        account.setTimestamp(ZonedDateTime.now().minusDays(1));
        account.setTradingDay(LocalDate.now().minusDays(1));
        account.setWithdraw(0.0D);
    }

    private void setCommissions() {
        // Order ID = 1L/2L
        // Commission has been cleared at renew.

        // Order ID = 3L
        var r = requests().get(3L);
        var c = new Commission();

        c.setOrderId(r.getOrderId());
        c.setTradingDay(r.getTradingDay());
        c.setStatus(FeeStatus.DEALED);
        c.setTag(r.getTag());
        c.setTimestamp(ZonedDateTime.now());
        c.setContractId(4L);
        c.setCommissionId(4L);
        c.setCommission(algorithm().getCommission(r.getPrice(),
                                                  instrument(r.getInstrumentId()),
                                                  r.getOffset(),
                                                  null,
                                                  r.getTradingDay()));
        commissions.add(c);

        c = Utils.copy(c);
        c.setCommissionId(5L);
        c.setContractId(5L);
        commissions.add(c);

        // Order ID = 4L
        r = requests().get(4L);
        c = new Commission();

        c.setStatus(FeeStatus.FORZEN);
        c.setTag(r.getTag());
        c.setTimestamp(ZonedDateTime.now());
        c.setContractId(3L);
        c.setCommissionId(6L);
        c.setOrderId(r.getOrderId());
        c.setTradingDay(r.getTradingDay());
        c.setCommission(algorithm.getCommission(r.getPrice(),
                                                instrument(r.getInstrumentId()),
                                                r.getOffset(),
                                                getContractById(3L),
                                                r.getTradingDay()));
        commissions.add(c);

        c = Utils.copy(c);
        c.setContractId(4L);
        c.setCommissionId(7L);
        c.setCommission(algorithm.getCommission(r.getPrice(),
                                                instrument(r.getInstrumentId()),
                                                r.getOffset(),
                                                getContractById(4L),
                                                r.getTradingDay()));
        commissions.add(c);

        // Order ID = 5L
        r = requests().get(5L);
        c = new Commission();

        c.setStatus(FeeStatus.DEALED);
        c.setTag(r.getTag());
        c.setTimestamp(ZonedDateTime.now());
        c.setContractId(2L);
        c.setCommissionId(8L);
        c.setOrderId(r.getOrderId());
        c.setTradingDay(r.getTradingDay());
        c.setCommission(algorithm.getCommission(r.getPrice(),
                                                instrument(r.getInstrumentId()),
                                                r.getOffset(),
                                                getContractById(2L),
                                                r.getTradingDay()));
        commissions.add(c);

        // Order ID = 6L
        r = requests().get(6L);
        c = new Commission();

        c.setStatus(FeeStatus.DEALED);
        c.setTag(r.getTag());
        c.setTimestamp(ZonedDateTime.now());
        c.setContractId(6L);
        c.setCommissionId(9L);
        c.setOrderId(r.getOrderId());
        c.setTradingDay(r.getTradingDay());
        c.setCommission(algorithm.getCommission(r.getPrice(),
                                                instrument(r.getInstrumentId()),
                                                r.getOffset(),
                                                getContractById(6L),
                                                r.getTradingDay()));
        commissions.add(c);

        // Order ID = 7L
        r = requests().get(7L);
        c = new Commission();

        c.setStatus(FeeStatus.DEALED);
        c.setTag(r.getTag());
        c.setTimestamp(ZonedDateTime.now());
        c.setContractId(6L);
        c.setCommissionId(10L);
        c.setOrderId(r.getOrderId());
        c.setTradingDay(r.getTradingDay());
        c.setCommission(algorithm.getCommission(r.getPrice(),
                                                instrument(r.getInstrumentId()),
                                                r.getOffset(),
                                                getContractById(6L),
                                                r.getTradingDay()));
        commissions.add(c);
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

        // Order ID = 5L
        c = Utils.copy(c);
        c.setContractId(2L);
        c.setStatus(ContractStatus.CLOSED);
        c.setCloseAmount(29450.0);
        c.setCloseTradingDay(LocalDate.now());
        c.setTimestamp(ZonedDateTime.now());
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

        // Order ID = 6L
        c = new Contract();
        c.setInstrumentId("c2105");
        c.setDirection(Direction.SELL);
        c.setTimestamp(ZonedDateTime.now().minusMinutes(6));
        c.setTag("sell-open-request");
        c.setContractId(6L);
        c.setCloseAmount(null);
        c.setCloseTradingDay(null);
        c.setOpenAmount(29550.0);
        c.setOpenTimestamp(ZonedDateTime.now().minusMinutes(7));
        c.setOpenTradingDay(LocalDate.now());
        c.setCloseAmount(29500.0);
        c.setCloseTradingDay(LocalDate.now());
        c.setStatus(ContractStatus.CLOSED);
        c.setTradeId(5L);
        c.setTraderId(3);
        contracts.add(c);
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

    private void setInstruments() {
        Instrument i = new Instrument();

        i.setCommissionCloseYdRatio(0.0);
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
        i.setCommissionCloseYdRatio(0.0);
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

    private void setMargins() {
        // Order ID = 1L
        var r = requests().get(1L);
        var m = new Margin();

        m.setContractId(1L);
        m.setStatus(FeeStatus.DEALED);
        m.setTimestamp(ZonedDateTime.now());
        m.setTag(r.getTag());
        m.setOrderId(r.getOrderId());
        m.setTradingDay(r.getTradingDay());
        m.setMarginId(1L);
        m.setMargin(algorithm().getMargin(r.getPrice(),
                                          instrument(r.getInstrumentId())));
        margins.add(m);

        // Order ID = 5L
        m = Utils.copy(m);
        m.setContractId(2L);
        m.setMarginId(2L);
        m.setStatus(FeeStatus.REMOVED);
        margins.add(m);

        // OrderID = 2L
        r = requests().get(2L);
        m = new Margin();

        m.setContractId(3L);
        m.setTimestamp(ZonedDateTime.now());
        m.setTag(r.getTag());
        m.setOrderId(r.getOrderId());
        m.setTradingDay(r.getTradingDay());
        m.setStatus(FeeStatus.DEALED);
        m.setMarginId(3L);
        m.setMargin(algorithm().getMargin(r.getPrice(),
                                          instrument(r.getInstrumentId())));
        margins.add(m);

        // Order ID = 3L
        r = requests().get(3L);
        m = new Margin();

        m.setContractId(4L);
        m.setTimestamp(ZonedDateTime.now());
        m.setTag(r.getTag());
        m.setOrderId(r.getOrderId());
        m.setTradingDay(r.getTradingDay());
        m.setStatus(FeeStatus.DEALED);
        m.setMarginId(4L);
        m.setMargin(algorithm().getMargin(r.getPrice(),
                                          instrument(r.getInstrumentId())));
        margins.add(m);

        m = Utils.copy(m);
        m.setContractId(5L);
        m.setMarginId(5L);
        margins.add(m);

        // Order ID = 6L
        r = requests().get(6L);
        m = new Margin();

        m.setContractId(6L);
        m.setTimestamp(ZonedDateTime.now());
        m.setTag(r.getTag());
        m.setOrderId(r.getOrderId());
        m.setTradingDay(r.getTradingDay());
        m.setStatus(FeeStatus.REMOVED);
        m.setMarginId(6L);
        m.setMargin(algorithm().getMargin(r.getPrice(),
                                          instrument(r.getInstrumentId())));
        margins.add(m);
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
        r.setOffset(Offset.CLOSE_AUTO);
        r.setAction(ActionType.NEW);
        r.setExchangeId("DCE");
        r.setInstrumentId("c2105");
        r.setRequestId(4L);
        r.setTradingDay(LocalDate.now());
        requests.put(r.getRequestId(),
                     r);

        r = new Request();

        r.setUpdateTimestamp(ZonedDateTime.now().minusMinutes(3));
        r.setTag("sell-close-request");
        r.setSignature(Utils.nextUuid().toString());
        r.setQuantity(1L);
        r.setPrice(2945.0);
        r.setTraderId(null);
        r.setDirection(Direction.SELL);
        r.setOrderId(5L);
        r.setOffset(Offset.CLOSE_YD);
        r.setAction(ActionType.NEW);
        r.setExchangeId("DCE");
        r.setInstrumentId("c2105");
        r.setRequestId(5L);
        r.setTradingDay(LocalDate.now());
        requests.put(r.getRequestId(),
                     r);

        r = new Request();

        r.setUpdateTimestamp(ZonedDateTime.now().minusMinutes(3));
        r.setTag("sell-open-request");
        r.setSignature(Utils.nextUuid().toString());
        r.setQuantity(1L);
        r.setPrice(2955.0);
        r.setTraderId(null);
        r.setDirection(Direction.SELL);
        r.setOrderId(6L);
        r.setOffset(Offset.OPEN);
        r.setAction(ActionType.NEW);
        r.setExchangeId("DCE");
        r.setInstrumentId("c2105");
        r.setRequestId(6L);
        r.setTradingDay(LocalDate.now());
        requests.put(r.getRequestId(),
                     r);

        r = new Request();

        r.setUpdateTimestamp(ZonedDateTime.now().minusMinutes(1));
        r.setTag("sell-open-request");
        r.setSignature(Utils.nextUuid().toString());
        r.setQuantity(1L);
        r.setPrice(2950.0);
        r.setTraderId(null);
        r.setDirection(Direction.BUY);
        r.setOrderId(7L);
        r.setOffset(Offset.CLOSE_TODAY);
        r.setAction(ActionType.NEW);
        r.setExchangeId("DCE");
        r.setInstrumentId("c2105");
        r.setRequestId(7L);
        r.setTradingDay(LocalDate.now());
        requests.put(r.getRequestId(),
                     r);
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
        r.setOffset(Offset.CLOSE_YD);
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

        // Order ID = 5L
        r = new Response();

        r.setAction(ActionType.NEW);
        r.setDirection(Direction.SELL);
        r.setOffset(Offset.CLOSE_YD);
        r.setSignature(Utils.nextUuid().toString());
        r.setStatus(OrderStatus.ACCEPTED);
        r.setInstrumentId("c2105");
        r.setOrderId(5L);
        r.setResponseId(9L);
        r.setStatusMessage("ACCEPTED");
        r.setTimestamp(ZonedDateTime.now().minusMinutes(3));
        r.setTraderId(1);
        r.setTradingDay(LocalDate.now());
        responses.add(r);

        r = Utils.copy(r);

        r.setTraderId(1);
        r.setResponseId(10L);
        r.setStatus(OrderStatus.ALL_TRADED);
        r.setStatusMessage("ALL_TRADED");
        r.setTimestamp(r.getTimestamp().plusSeconds(1));
        responses.add(r);

        // Order ID = 6L
        r = new Response();

        r.setAction(ActionType.NEW);
        r.setDirection(Direction.SELL);
        r.setOffset(Offset.OPEN);
        r.setSignature(Utils.nextUuid().toString());
        r.setStatus(OrderStatus.ACCEPTED);
        r.setInstrumentId("c2105");
        r.setOrderId(6L);
        r.setResponseId(11L);
        r.setStatusMessage("ACCEPTED");
        r.setTimestamp(ZonedDateTime.now().minusMinutes(1));
        r.setTraderId(3);
        r.setTradingDay(LocalDate.now());
        responses.add(r);

        r = Utils.copy(r);

        r.setTraderId(3);
        r.setResponseId(12L);
        r.setStatus(OrderStatus.ALL_TRADED);
        r.setStatusMessage("ALL_TRADED");
        r.setTimestamp(r.getTimestamp().plusSeconds(1));
        responses.add(r);

        // Order ID = 7L
        r = new Response();

        r.setAction(ActionType.NEW);
        r.setDirection(Direction.BUY);
        r.setOffset(Offset.CLOSE_TODAY);
        r.setSignature(Utils.nextUuid().toString());
        r.setStatus(OrderStatus.ACCEPTED);
        r.setInstrumentId("c2105");
        r.setOrderId(7L);
        r.setResponseId(13L);
        r.setStatusMessage("ACCEPTED");
        r.setTimestamp(ZonedDateTime.now().minusMinutes(1));
        r.setTraderId(3);
        r.setTradingDay(LocalDate.now());
        responses.add(r);

        r = Utils.copy(r);

        r.setTraderId(3);
        r.setResponseId(14L);
        r.setStatus(OrderStatus.ALL_TRADED);
        r.setStatusMessage("ALL_TRADED");
        r.setTimestamp(r.getTimestamp().plusSeconds(1));
        responses.add(r);
    }

    private void setSettlements() {
        var sp0 = new SettlementPrice();
        sp0.setInstrumentId("c2105");
        sp0.setTimestamp(ZonedDateTime.now());
        sp0.setTradingDay(LocalDate.now());
        sp0.setSettlementPrice(2970.0);
        sp0.setSettlementPriceId(1L);
        settlements.put(sp0.getInstrumentId(),
                        sp0);
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

        // Order ID = 5L
        t = Utils.copy(t);
        t.setAction(ActionType.NEW);
        t.setDirection(Direction.SELL);
        t.setOffset(Offset.CLOSE_YD);
        t.setInstrumentId("c2105");
        t.setOrderId(5L);
        t.setTraderId(1);
        t.setTimestamp(ZonedDateTime.now());
        t.setTradingDay(LocalDate.now());
        t.setTradeId(4L);
        t.setPrice(2945.0);
        t.setQuantity(1L);
        t.setSignature(Utils.nextUuid().toString());
        trades.add(t);

        // Order ID = 6L
        t = Utils.copy(t);
        t.setAction(ActionType.NEW);
        t.setDirection(Direction.SELL);
        t.setOffset(Offset.OPEN);
        t.setInstrumentId("c2105");
        t.setOrderId(6L);
        t.setTraderId(3);
        t.setTimestamp(ZonedDateTime.now().minusMinutes(1));
        t.setTradingDay(LocalDate.now());
        t.setTradeId(5L);
        t.setPrice(2955.0);
        t.setQuantity(1L);
        t.setSignature(Utils.nextUuid().toString());
        trades.add(t);

        // Order ID = 7L
        t = Utils.copy(t);
        t.setAction(ActionType.NEW);
        t.setDirection(Direction.BUY);
        t.setOffset(Offset.CLOSE_TODAY);
        t.setInstrumentId("c2105");
        t.setOrderId(7L);
        t.setTraderId(3);
        t.setTimestamp(ZonedDateTime.now().minusMinutes(1));
        t.setTradingDay(LocalDate.now());
        t.setTradeId(6L);
        t.setPrice(2950.0);
        t.setQuantity(1L);
        t.setSignature(Utils.nextUuid().toString());
        trades.add(t);
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
}
