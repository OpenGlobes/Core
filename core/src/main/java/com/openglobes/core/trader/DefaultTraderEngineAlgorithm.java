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

import java.time.LocalDate;
import java.util.*;

/**
 * Algorithm implemetation.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class DefaultTraderEngineAlgorithm implements ITraderEngineAlgorithm {

    private final Properties props;

    public DefaultTraderEngineAlgorithm() {
        props = new Properties();
    }

    @Override
    public Account getAccount(Account pre,
                              Collection<Deposit> deposits,
                              Collection<Withdraw> withdraws,
                              Collection<Position> positions) throws InvalidAmountException {
        double closeProfit      = 0D;
        double positionProfit   = 0D;
        double frozenMargin     = 0D;
        double frozenCommission = 0D;
        double margin           = 0D;
        double commission       = 0D;
        Objects.requireNonNull(positions);
        for (var p : positions) {
            requireNotNulls(p.getCloseProfit(),
                            p.getPositionProfit(),
                            p.getFrozenMargin(),
                            p.getFrozenCommission(),
                            p.getMargin(),
                            p.getCommission());
            closeProfit += p.getCloseProfit();
            positionProfit += p.getPositionProfit();
            frozenMargin += p.getFrozenMargin();
            frozenCommission += p.getFrozenCommission();
            margin += p.getMargin();
            commission += p.getCommission();
        }
        double deposit  = getProperDeposit(deposits);
        double withdraw = getProperWithdraw(withdraws);
        var    r        = initAccount(pre);
        r.setCloseProfit(closeProfit);
        r.setCommission(commission);
        r.setDeposit(deposit);
        r.setFrozenCommission(frozenCommission);
        r.setFrozenMargin(frozenMargin);
        r.setMargin(margin);
        r.setPositionProfit(positionProfit);
        r.setWithdraw(withdraw);
        var balance = r.getPreBalance() + r.getDeposit() - r.getWithdraw()
                      + r.getCloseProfit() + r.getPositionProfit() - r.getCommission();
        r.setBalance(balance);
        return r;
    }

    @Override
    public double getAmount(double price, Instrument instrument) {
        Objects.requireNonNull(instrument,
                               instrument.getInstrumentId());
        var multiple = instrument.getMultiple();
        Objects.requireNonNull(multiple,
                               instrument.getInstrumentId() + " multiple.");
        return price * multiple;
    }

    @Override
    public double getCommission(double price,
                                Instrument instrument,
                                Integer direction,
                                Integer offset) {
        Objects.requireNonNull(instrument,
                               instrument.getInstrumentId());
        var ctype = instrument.getCommissionType();
        Objects.requireNonNull(ctype,
                               instrument.getInstrumentId() + " commission type.");
        var ratio = getProperCommissionRatio(instrument, offset);
        Objects.requireNonNull(ratio,
                               instrument.getInstrumentId() + " commission ratio.");
        if (ctype == RatioType.BY_MONEY) {
            double m = getAmount(price,
                                 instrument);
            return m * ratio;
        } else {
            return ratio;
        }
    }

    @Override
    public double getMargin(double price,
                            Instrument instrument) {
        Objects.requireNonNull(instrument,
                               instrument.getInstrumentId());
        var type = instrument.getMarginType();
        Objects.requireNonNull(type,
                               instrument.getInstrumentId() + " margin type.");
        var ratio = instrument.getMarginRatio();
        Objects.requireNonNull(ratio,
                               instrument.getInstrumentId() + " margin ratio.");
        if (type == RatioType.BY_MONEY) {
            double m = getAmount(price,
                                 instrument);
            return m * ratio;
        } else {
            return ratio;
        }
    }

    @Override
    public Order getOrder(Request request,
                          Collection<Contract> contracts,
                          Collection<Trade> trades,
                          Collection<Response> responses) throws IllegalContractException,
                                                                 QuantityOverflowException {
        var r = new Order();
        /*
         * Don't change the order of calls.
         */
        setRequests(r,
                    request);
        setContracts(r,
                     contracts);
        setTrades(r,
                  trades);
        setDeleted(r,
                   responses);
        setOrderStatus(r);
        return r;
    }

    @Override
    public Collection<Position> getPositions(Collection<Contract> contracts,
                                             Collection<Commission> commissions,
                                             Collection<Margin> margins,
                                             Map<String, SettlementPrice> prices,
                                             Map<String, Instrument> instruments,
                                             LocalDate tradingDay) throws InvalidContractDirectionException,
                                                                          IllegalInstrumentIdException,
                                                                          InvalidContractIdException,
                                                                          MarginNotFoundException,
                                                                          CommissionNotFoundException,
                                                                          SettlementNotFoundException,
                                                                          InvalidSettlementPriceException,
                                                                          InstrumentNotFoundException,
                                                                          InvalidContractStatusException,
                                                                          IllegalContractStatusException,
                                                                          IllegalFeeStatusException,
                                                                          InvalidCommissionException {
        Objects.requireNonNull(contracts);
        final var lp = new HashMap<String, Position>(64);
        final var sp = new HashMap<String, Position>(64);
        /*
         * Store margins/commissions in map for constant access time.
         */
        final var map  = new HashMap<Long, Margin>(64);
        final var cmap = new HashMap<Long, Set<Commission>>(64);
        margins.forEach(m -> {
            map.put(m.getContractId(), m);
        });
        commissions.forEach(c -> {
            var s = cmap.computeIfAbsent(c.getContractId(), k -> new HashSet<>(2));
            s.add(c);
        });

        for (var c : contracts) {
            Position p;
            var      direction = c.getDirection();
            if (direction == null) {
                throw new InvalidContractDirectionException(c.getInstrumentId());
            }
            if (direction == Direction.BUY) {
                p = lp.computeIfAbsent(c.getInstrumentId(), k -> {
                    return initPosition(c, tradingDay);
                });
            } else {
                p = sp.computeIfAbsent(c.getInstrumentId(), k -> {
                    return initPosition(c, tradingDay);
                });
            }
            var id = c.getInstrumentId();
            if (id.isBlank()) {
                throw new IllegalInstrumentIdException("Empty string.");
            }
            var cid = c.getContractId();
            if (cid == null) {
                throw new InvalidContractIdException("Contract ID null ptr.");
            }
            var margin = findMargin(cid,
                                    map);
            var commission = findCommission(cid,
                                            cmap);
            var price = findPriceProperty(id,
                                          prices);
            var instrument = findInstrumentProperty(id,
                                                    instruments);
            if (c.getOpenTradingDay().isBefore(tradingDay)) {
                addPrePosition(p,
                               c,
                               commission,
                               margin,
                               price,
                               instrument);
            } else {
                addTodayPosition(p,
                                 c,
                                 commission,
                                 margin,
                                 price,
                                 instrument);
            }
        }
        var r = new HashSet<Position>(lp.values());
        r.addAll(sp.values());
        return r;
    }

    @Override
    public void setProperties(Properties properties) {
        props.clear();
        props.putAll(properties);
    }

    private void addClosedContract(Position p,
                                   Contract c,
                                   Collection<Commission> commissions) throws InvalidCommissionException {
        var closeProfit = getProperProfit(c.getOpenAmount(),
                                          c.getCloseAmount(),
                                          c.getDirection());
        if (p.getCloseProfit() == null) {
            p.setCloseProfit(closeProfit);
        } else {
            p.setCloseProfit(p.getCloseProfit() + closeProfit);
        }
        var commission = getProperCommission(c.getContractId(),
                                             commissions,
                                             FeeStatus.DEALED);
        if (p.getCommission() == null) {
            p.setCommission(commission);
        } else {
            p.setCommission(p.getCommission() + commission);
        }
    }

    private void addClosingContract(Position p,
                                    Contract c,
                                    Collection<Commission> commissions,
                                    Margin margin,
                                    Double price,
                                    Instrument instrument) throws InvalidCommissionException,
                                                                  IllegalContractStatusException,
                                                                  IllegalFeeStatusException {
        if (p.getAmount() == null) {
            p.setAmount(c.getOpenAmount());
        } else {
            p.setAmount(p.getAmount() + c.getOpenAmount());
        }
        Double commission = getProperCommission(c.getContractId(),
                                                commissions,
                                                FeeStatus.DEALED);
        Double frozenCommission = getProperCommission(c.getContractId(),
                                                      commissions,
                                                      FeeStatus.FORZEN);
        if (p.getCommission() == null) {
            p.setCommission(commission);
        } else {
            p.setCommission(p.getCommission() + commission);
        }
        if (p.getFrozenCommission() == null) {
            p.setFrozenCommission(frozenCommission);
        } else {
            p.setFrozenCommission(p.getFrozenCommission() + frozenCommission);
        }
        Long frozenCloseVolumn = getProperVolumn(c.getStatus(),
                                                 ContractStatus.CLOSING);
        var volumn = frozenCloseVolumn;
        if (p.getVolumn() == null) {
            p.setVolumn(volumn);
        } else {
            p.setVolumn(p.getVolumn() + volumn);
        }
        if (p.getFrozenCloseVolumn() == null) {
            p.setFrozenCloseVolumn(frozenCloseVolumn);
        } else {
            p.setFrozenCloseVolumn(p.getFrozenCloseVolumn() + frozenCloseVolumn);
        }
        Double m = getProperMargin(c.getContractId(),
                                   margin,
                                   FeeStatus.DEALED);
        if (p.getMargin() == null) {
            p.setMargin(m);
        } else {
            p.setMargin(p.getMargin() + m);
        }
        Double pprofit = getProperPositionProfit(c,
                                                 price,
                                                 instrument);
        if (p.getPositionProfit() == null) {
            p.setPositionProfit(pprofit);
        } else {
            p.setPositionProfit(p.getPositionProfit() + pprofit);
        }

    }

    private void addOpenContract(Position p,
                                 Contract c,
                                 Collection<Commission> commissions,
                                 Margin margin,
                                 Double price,
                                 Instrument instrument) throws InvalidCommissionException, IllegalContractStatusException, IllegalFeeStatusException {
        if (p.getAmount() == null) {
            p.setAmount(c.getOpenAmount());
        } else {
            p.setAmount(p.getAmount() + c.getOpenAmount());
        }
        Double commission = getProperCommission(c.getContractId(),
                                                commissions,
                                                FeeStatus.DEALED);
        if (p.getCommission() == null) {
            p.setCommission(commission);
        } else {
            p.setCommission(p.getCommission() + commission);
        }
        Long volumn = getProperVolumn(c.getStatus(),
                                      ContractStatus.OPEN);
        if (p.getVolumn() == null) {
            p.setVolumn(volumn);
        } else {
            p.setVolumn(p.getVolumn() + volumn);
        }
        Double m = getProperMargin(c.getContractId(),
                                   margin,
                                   FeeStatus.DEALED);
        if (p.getMargin() == null) {
            p.setMargin(m);
        } else {
            p.setMargin(p.getMargin() + m);
        }
        Double pprofit = getProperPositionProfit(c,
                                                 price,
                                                 instrument);
        if (p.getPositionProfit() == null) {
            p.setPositionProfit(pprofit);
        } else {
            p.setPositionProfit(p.getPositionProfit() + pprofit);
        }
    }

    private void addOpeningContract(Position p,
                                    Contract c,
                                    Collection<Commission> commissions,
                                    Margin margin) throws InvalidCommissionException,
                                                          IllegalContractStatusException,
                                                          IllegalFeeStatusException {
        Double frozenCommission = getProperCommission(c.getContractId(),
                                                      commissions,
                                                      FeeStatus.FORZEN);
        if (p.getFrozenCommission() == null) {
            p.setFrozenCommission(frozenCommission);
        } else {
            p.setFrozenCommission(p.getFrozenCommission() + frozenCommission);
        }
        Long frozenOpenVolumn = getProperVolumn(c.getStatus(),
                                                ContractStatus.OPENING);
        if (p.getFrozenOpenVolumn() == null) {
            p.setFrozenOpenVolumn(frozenOpenVolumn);
        } else {
            p.setFrozenOpenVolumn(p.getFrozenOpenVolumn() + frozenOpenVolumn);
        }
        Double frozenMargin = getProperMargin(c.getContractId(),
                                              margin,
                                              FeeStatus.FORZEN);
        if (p.getFrozenMargin() == null) {
            p.setFrozenMargin(frozenMargin);
        } else {
            p.setFrozenMargin(p.getFrozenMargin() + frozenMargin);
        }
    }

    private void addPreContract(Position p, Contract c, Margin margin) {
        if (p.getPreAmount() == null) {
            p.setPreAmount(c.getOpenAmount());
        } else {
            p.setPreAmount(p.getPreAmount() + c.getOpenAmount());
        }
        if (p.getPreVolumn() == null) {
            p.setPreVolumn(1L);
        } else {
            p.setPreVolumn(p.getPreVolumn() + 1L);
        }
        if (p.getPreMargin() == null) {
            p.setPreMargin(margin.getMargin());
        } else {
            p.setPreMargin(p.getPreMargin() + margin.getMargin());
        }
    }

    private void addPrePosition(Position p,
                                Contract c,
                                Collection<Commission> commissions,
                                Margin margin,
                                Double price,
                                Instrument instrument) throws InvalidContractStatusException,
                                                              IllegalContractStatusException,
                                                              InvalidCommissionException,
                                                              IllegalFeeStatusException {
        var status = c.getStatus();
        if (status == null) {
            throw new InvalidContractStatusException("Contract ID:" + c.getContractId() + ".");
        }
        switch (status) {
            case ContractStatus.CLOSED:
                addClosedContract(p,
                                  c,
                                  commissions);
                break;
            case ContractStatus.CLOSING:
                addClosingContract(p,
                                   c,
                                   commissions,
                                   margin,
                                   price,
                                   instrument);
                break;
            case ContractStatus.OPEN:
                addOpenContract(p,
                                c,
                                commissions,
                                margin,
                                price,
                                instrument);
                break;
            default:
                throw new IllegalContractStatusException("Contract ID:" + c.getContractId() + ".");
        }
        addPreContract(p,
                       c,
                       margin);
    }

    private void addTodayContract(Position p,
                                  Contract c,
                                  Margin margin) throws IllegalFeeStatusException,
                                                        IllegalContractStatusException {
        if (p.getTodayAmount() == null) {
            p.setTodayAmount(c.getOpenAmount());
        } else {
            p.setTodayAmount(p.getTodayAmount() + c.getOpenAmount());
        }
        var volumn = getProperVolumn(c.getStatus(),
                                     ContractStatus.OPEN);
        if (p.getTodayVolumn() == null) {
            p.setTodayVolumn(volumn);
        } else {
            p.setTodayVolumn(p.getTodayVolumn() + volumn);
        }
        var m = getProperMargin(c.getContractId(),
                                margin,
                                FeeStatus.DEALED);
        if (p.getTodayMargin() == null) {
            p.setTodayMargin(m);
        } else {
            p.setTodayMargin(p.getTodayMargin() + m);
        }
    }

    private void addTodayOpenContract(Position p,
                                      Contract c,
                                      Margin margin) {
        if (p.getTodayOpenAmount() == null) {
            p.setTodayOpenAmount(c.getOpenAmount());
        } else {
            p.setTodayOpenAmount(p.getTodayOpenAmount() + c.getOpenAmount());
        }
        if (p.getTodayOpenVolumn() == null) {
            p.setTodayOpenVolumn(1L);
        } else {
            p.setTodayOpenVolumn(p.getTodayOpenVolumn() + 1L);
        }
        if (p.getTodayOpenMargin() == null) {
            p.setTodayOpenMargin(margin.getMargin());
        } else {
            p.setTodayOpenMargin(p.getTodayOpenMargin() + margin.getMargin());
        }
    }

    private void addTodayPosition(Position p,
                                  Contract c,
                                  Collection<Commission> commissions,
                                  Margin margin,
                                  Double price,
                                  Instrument instrument) throws InvalidContractStatusException,
                                                                IllegalContractStatusException,
                                                                IllegalFeeStatusException,
                                                                InvalidCommissionException {
        var status = c.getStatus();
        if (status == null) {
            throw new InvalidContractStatusException("Contract ID:" + c.getContractId() + ".");
        }
        switch (status) {
            case ContractStatus.CLOSED:
                addClosedContract(p,
                                  c,
                                  commissions);
                addTodayOpenContract(p,
                                     c,
                                     margin);
                break;
            case ContractStatus.OPENING:
                addOpeningContract(p,
                                   c,
                                   commissions,
                                   margin);
                break;
            case ContractStatus.CLOSING:
                addClosingContract(p,
                                   c,
                                   commissions,
                                   margin,
                                   price,
                                   instrument);
                addTodayContract(p,
                                 c,
                                 margin);
                addTodayOpenContract(p,
                                     c,
                                     margin);
                break;
            case ContractStatus.OPEN: // OPEN
                addOpenContract(p,
                                c,
                                commissions,
                                margin,
                                price,
                                instrument);
                addTodayContract(p,
                                 c,
                                 margin);
                addTodayOpenContract(p,
                                     c,
                                     margin);
                break;
            default:
                throw new IllegalContractStatusException("Contract ID:" + c.getContractId() + ")");
        }
    }

    private Collection<Commission> findCommission(Long contractId,
                                                  Map<Long, Set<Commission>> commissions) throws CommissionNotFoundException {
        var v = commissions.get(contractId);
        if (v == null) {
            throw new CommissionNotFoundException("Contract ID:" + contractId + ".");
        }
        return v;
    }

    private Instrument findInstrumentProperty(String key,
                                              Map<String, Instrument> instruments) throws InstrumentNotFoundException {
        var v = instruments.get(key);
        if (v == null) {
            throw new InstrumentNotFoundException(key);
        }
        return v;
    }

    private Margin findMargin(Long contractId,
                              Map<Long, Margin> margins) throws MarginNotFoundException {
        var v = margins.get(contractId);
        if (v == null) {
            throw new MarginNotFoundException("Contract ID:" + contractId + ".");
        }
        return v;
    }

    private double findPriceProperty(String key,
                                     Map<String, SettlementPrice> prices) throws SettlementNotFoundException,
                                                                                 InvalidSettlementPriceException {
        var v = prices.get(key);
        if (v == null) {
            throw new SettlementNotFoundException(key);
        }
        var p = v.getSettlementPrice();
        if (p == null) {
            throw new InvalidSettlementPriceException(key);
        }
        return p;
    }

    private void setOrderStatus(Order order) throws QuantityOverflowException {
        if (order.getStatus() != null) {
            return;
        }
        var traded = order.getTradedVolumn();
        if (traded > order.getQuantity()) {
            throw new QuantityOverflowException("Order ID:" + order.getOrderId() + ").");
        }
        if (traded > 0) {
            if (Objects.equals(traded, order.getQuantity())) {
                order.setStatus(OrderStatus.ALL_TRADED);
            } else {
                order.setStatus(OrderStatus.QUEUED);
            }
        } else {
            order.setStatus(OrderStatus.ACCEPTED);
        }
    }

    private double getProperCommission(Long contractId,
                                       Collection<Commission> commissions,
                                       Integer status) throws InvalidCommissionException {
        double v = 0D;
        for (var c : commissions) {
            if (Objects.equals(c.getContractId(), contractId)
                && Objects.equals(c.getStatus(), status)) {
                var x = c.getCommission();
                if (x == null) {
                    throw new InvalidCommissionException("Commission null ptr.");
                }
                v += x;
            }
        }
        return v;
    }

    private Double getProperCommissionRatio(Instrument instrument,
                                            Integer offset) {
        Objects.requireNonNull(instrument,
                               instrument.getInstrumentId());
        if (offset == Offset.OPEN) {
            return instrument.getCommissionOpenRatio();
        } else {
            if (offset == Offset.CLOSE) {
                return instrument.getCommissionCloseRatio();
            } else {
                return instrument.getCommissionCloseTodayRatio();
            }
        }
    }

    private double getProperDeposit(Collection<Deposit> deposits) throws InvalidAmountException {
        Objects.requireNonNull(deposits);
        double deposit = 0D;
        for (var d : deposits) {
            var a = d.getAmount();
            if (a == null) {
                throw new InvalidAmountException("Amount null ptr.");
            }
            deposit += d.getAmount();
        }
        return deposit;
    }

    private double getProperMargin(Long contractId,
                                   Margin margin,
                                   Integer status) throws IllegalFeeStatusException {
        if (Objects.equals(contractId, margin.getContractId())
            && Objects.equals(margin.getStatus(), status)) {
            return margin.getMargin();
        } else {
            throw new IllegalFeeStatusException(contractId.toString());
        }
    }

    private double getProperPositionProfit(Contract c,
                                           Double price,
                                           Instrument instrument) {
        var a = getAmount(price, instrument);
        return getProperProfit(c.getOpenAmount(),
                               a,
                               c.getDirection());
    }

    private double getProperProfit(double pre,
                                   double current,
                                   Integer direction) {
        Objects.requireNonNull(direction);
        if (direction == Direction.BUY) {
            return current - pre;
        } else {
            return pre - current;
        }
    }

    private long getProperVolumn(Integer status,
                                 Integer wantedStatus) throws IllegalContractStatusException {
        if (Objects.equals(status, wantedStatus)) {
            return 1L;
        } else {
            throw new IllegalContractStatusException("Unexpected contract status.");
        }
    }

    private double getProperWithdraw(Collection<Withdraw> withdraws) throws InvalidAmountException {
        Objects.requireNonNull(withdraws);
        double withdraw = 0D;
        for (var w : withdraws) {
            var a = w.getAmount();
            if (a == null) {
                throw new InvalidAmountException("Withdraw ID: " + w.getWithdrawId() + ".");
            }
            withdraw += w.getAmount();
        }
        return withdraw;
    }

    private Account initAccount(Account a) {
        var r = new Account();
        r.setBalance(0D);
        r.setCloseProfit(0D);
        r.setCommission(0D);
        r.setDeposit(0D);
        r.setFrozenCommission(0D);
        r.setFrozenMargin(0D);
        r.setMargin(0D);
        r.setPositionProfit(0D);
        r.setPreBalance(a.getBalance());
        r.setPreDeposit(a.getDeposit());
        r.setPreMargin(a.getMargin());
        r.setPreWithdraw(a.getWithdraw());
        r.setWithdraw(0D);
        return r;
    }

    private Position initPosition(Contract c, LocalDate tradingDay) {
        var p0 = new Position();
        p0.setAmount(0.0D);
        p0.setCloseProfit(0.0D);
        p0.setFrozenCloseVolumn(0L);
        p0.setFrozenMargin(0.0D);
        p0.setFrozenOpenVolumn(0L);
        p0.setInstrumentId(c.getInstrumentId());
        p0.setMargin(0.0D);
        p0.setPositionProfit(0.0D);
        p0.setPreAmount(0.0D);
        p0.setPreMargin(0.0D);
        p0.setPreVolumn(0L);
        p0.setTodayAmount(0.0D);
        p0.setTodayMargin(0.0D);
        p0.setTodayVolumn(0L);
        p0.setTradingDay(tradingDay);
        p0.setDirection(c.getDirection());
        p0.setVolumn(0L);
        return p0;
    }

    private void requireNotNulls(Object... values) {
        for (var v : values) {
            if (v == null) {
                Objects.requireNonNull(v);
            }
        }
    }

    private void setContracts(Order order,
                              Collection<Contract> contracts) throws IllegalContractException {
        double amount       = 0D;
        long   tradedVolumn = 0L;
        for (var c : contracts) {
            if (!Objects.equals(c.getDirection(), order.getDirection())
                || c.getInstrumentId().equals(order.getInstrumentId())) {
                throw new IllegalContractException("Unexpected values.");
            }
            amount += c.getOpenAmount();
            ++tradedVolumn;
        }
        order.setAmount(amount);
        order.setTradedVolumn(tradedVolumn);
    }

    private void setDeleted(Order order,
                            Collection<Response> responses) {
        Objects.requireNonNull(responses);
        if (responses.isEmpty()) {
            order.setDeleted(Boolean.FALSE);
        } else {
            for (var r : responses) {
                if (r.getStatus() == OrderStatus.DELETED) {
                    order.setDeleteTimestamp(r.getTimestamp());
                    order.setDeleted(Boolean.TRUE);
                    order.setStatus(OrderStatus.DELETED);
                    order.setStatusCode(r.getStatusCode());
                    order.setStatusMessage(r.getStatusMessage());
                    return;
                }
            }
        }
    }

    private void setRequests(Order order, Request request) {
        order.setInstrumentId(request.getInstrumentId());
        order.setDirection(request.getDirection());
        order.setOffset(request.getOffset());
        order.setOrderId(request.getOrderId());
        order.setQuantity(request.getQuantity());
        order.setTraderId(request.getTraderId());
    }

    private void setTrades(Order order,
                           Collection<Trade> trades) {
        Objects.requireNonNull(trades);
        if (trades.isEmpty()) {
            return;
        }
        var ts = new LinkedList<Trade>(trades);
        ts.sort((Trade o1, Trade o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
        order.setTradingDay(ts.getFirst().getTradingDay());
        order.setInsertTimestamp(ts.getFirst().getTimestamp());
        order.setUpdateTimestamp(ts.getLast().getTimestamp());
    }
}
