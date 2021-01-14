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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

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
                              Collection<Position> positions) throws AlgorithmException {
        double closeProfit = 0D;
        double positionProfit = 0D;
        double frozenMargin = 0D;
        double frozenCommission = 0D;
        double margin = 0D;
        double commission = 0D;
        if (positions == null) {
            throw new AlgorithmException(ErrorCode.POSITION_NULL.code(),
                                         ErrorCode.POSITION_NULL.message());
        }
        for (var p : positions) {
            checkPositionFieldNotNull(p.getCloseProfit(),
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
        double deposit = getProperDeposit(deposits);
        double withdraw = getProperWithdraw(withdraws);
        var r = initAccount(pre);
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
    public double getAmount(double price, Instrument instrument) throws AlgorithmException {
        checkInstrument(instrument);
        var multiple = instrument.getMultiple();
        if (multiple == null) {
            throw new AlgorithmException(ErrorCode.MULTIPLE_NULL.code(),
                                         ErrorCode.MULTIPLE_NULL.message()
                                         + "(" + instrument.getInstrumentId() + ")");
        }
        return price * multiple;
    }

    @Override
    public double getCommission(double price,
                                Instrument instrument,
                                Integer direction,
                                Integer offset) throws AlgorithmException {
        checkInstrument(instrument);
        var ctype = instrument.getCommissionType();
        checkRatioType(ctype);
        var ratio = getProperCommissionRatio(instrument, offset);
        if (ratio == null) {
            throw new AlgorithmException(ErrorCode.RATIO_NULL.code(),
                                         ErrorCode.RATIO_NULL.message()
                                         + "(" + instrument.getInstrumentId() + ")");
        }
        if (ctype == RatioType.BY_MONEY) {
            return getAmount(price, instrument) * ratio;
        }
        else {
            return ratio;
        }
    }

    @Override
    public double getMargin(double price,
                            Instrument instrument) throws AlgorithmException {
        checkInstrument(instrument);
        var type = instrument.getMarginType();
        checkRatioType(type);
        var ratio = instrument.getMarginRatio();
        if (ratio == null) {
            throw new AlgorithmException(ErrorCode.RATIO_NULL.code(),
                                         ErrorCode.RATIO_NULL.message()
                                         + "(" + instrument.getInstrumentId() + ")");
        }
        if (type == RatioType.BY_MONEY) {
            return getAmount(price, instrument) * ratio;
        }
        else {
            return ratio;
        }
    }

    @Override
    public Order getOrder(Request request,
                          Collection<Contract> contracts,
                          Collection<Trade> trades,
                          Collection<Response> responses) throws AlgorithmException {
        var r = new Order();
        /*
         * Don't change the order of calls.
         */
        setRequests(r, request);
        setContracts(r, contracts);
        setTrades(r, trades);
        setDeleted(r, responses);
        setOrderStatus(r);
        return r;
    }

    @Override
    public Collection<Position> getPositions(Collection<Contract> contracts,
                                             Collection<Commission> commissions,
                                             Collection<Margin> margins,
                                             Map<String, SettlementPrice> prices,
                                             Map<String, Instrument> instruments,
                                             LocalDate tradingDay) throws AlgorithmException {
        if (contracts == null) {
            throw new AlgorithmException(ErrorCode.CONTRACT_NULL.code(),
                                         ErrorCode.CONTRACT_NULL.message());
        }
        final var lp = new HashMap<String, Position>(64);
        final var sp = new HashMap<String, Position>(64);
        /*
         * Store margins/commissions in map for constant access time.
         */
        final var map = new HashMap<Long, Margin>(64);
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
            var direction = c.getDirection();
            if (null == direction) {
                throw new AlgorithmException(ErrorCode.DIRECTION_NULL.code(),
                                             ErrorCode.DIRECTION_NULL.message());
            }
            if (direction == Direction.BUY) {
                p = lp.computeIfAbsent(c.getInstrumentId(), k -> {
                                   return initPosition(c, tradingDay);
                               });
            }
            else {
                p = sp.computeIfAbsent(c.getInstrumentId(), k -> {
                                   return initPosition(c, tradingDay);
                               });
            }
            var iid = c.getInstrumentId();
            checkInstrumentId(iid);
            var cid = c.getContractId();
            checkContractId(cid);
            var pk = iid + ".Price";
            var ik = iid + ".Instrument";
            var margin = findMargin(cid, map);
            var commission = findCommission(cid, cmap);
            var price = findPriceProperty(pk, prices);
            var instrument = findInstrumentProperty(ik, instruments);
            if (c.getOpenTradingDay().isBefore(tradingDay)) {
                addPrePosition(p,
                               c,
                               commission,
                               margin,
                               price,
                               instrument);
            }
            else {
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
    public void setProperties(Properties properties) throws AlgorithmException {
        props.clear();
        props.putAll(properties);
    }

    private void addClosedContract(Position p,
                                   Contract c,
                                   Collection<Commission> commissions) throws AlgorithmException {
        var closeProfit = getProperProfit(c.getOpenAmount(),
                                      c.getCloseAmount(),
                                      c.getDirection());
        if (p.getCloseProfit() == null) {
            p.setCloseProfit(closeProfit);
        }
        else {
            p.setCloseProfit(p.getCloseProfit() + closeProfit);
        }
        var commission = getProperCommission(c.getContractId(),
                                         commissions,
                                         FeeStatus.DEALED);
        if (p.getCommission() == null) {
            p.setCommission(commission);
        }
        else {
            p.setCommission(p.getCommission() + commission);
        }
    }

    private void addClosingContract(Position p,
                                    Contract c,
                                    Collection<Commission> commissions,
                                    Margin margin,
                                    Double price,
                                    Instrument instrument) throws AlgorithmException {
        if (p.getAmount() == null) {
            p.setAmount(c.getOpenAmount());
        }
        else {
            p.setAmount(p.getAmount() + c.getOpenAmount());
        }
        var commission = getProperCommission(c.getContractId(),
                                         commissions,
                                         FeeStatus.DEALED);
        var frozenCommission = getProperCommission(c.getContractId(),
                                               commissions,
                                               FeeStatus.FORZEN);
        if (p.getCommission() == null) {
            p.setCommission(commission);
        }
        else {
            p.setCommission(p.getCommission() + commission);
        }
        if (p.getFrozenCommission() == null) {
            p.setFrozenCommission(frozenCommission);
        }
        else {
            p.setFrozenCommission(p.getFrozenCommission() + frozenCommission);
        }
        var frozenCloseVolumn = getProperVolumn(c.getStatus(), ContractStatus.CLOSING);
        var volumn = frozenCloseVolumn;
        if (p.getVolumn() == null) {
            p.setVolumn(volumn);
        }
        else {
            p.setVolumn(p.getVolumn() + volumn);
        }
        if (p.getFrozenCloseVolumn() == null) {
            p.setFrozenCloseVolumn(frozenCloseVolumn);
        }
        else {
            p.setFrozenCloseVolumn(p.getFrozenCloseVolumn() + frozenCloseVolumn);
        }
        var m = getProperMargin(c.getContractId(),
                            margin,
                            FeeStatus.DEALED);
        if (p.getMargin() == null) {
            p.setMargin(m);
        }
        else {
            p.setMargin(p.getMargin() + m);
        }
        var pprofit = getProperPositionProfit(c,
                                          price,
                                          instrument);
        if (p.getPositionProfit() == null) {
            p.setPositionProfit(pprofit);
        }
        else {
            p.setPositionProfit(p.getPositionProfit() + pprofit);
        }

    }

    private void addOpenContract(Position p,
                                 Contract c,
                                 Collection<Commission> commissions,
                                 Margin margin,
                                 Double price,
                                 Instrument instrument) throws AlgorithmException {
        if (p.getAmount() == null) {
            p.setAmount(c.getOpenAmount());
        }
        else {
            p.setAmount(p.getAmount() + c.getOpenAmount());
        }
        var commission = getProperCommission(c.getContractId(),
                                         commissions,
                                         FeeStatus.DEALED);
        if (p.getCommission() == null) {
            p.setCommission(commission);
        }
        else {
            p.setCommission(p.getCommission() + commission);
        }
        var volumn = getProperVolumn(c.getStatus(), ContractStatus.OPEN);
        if (p.getVolumn() == null) {
            p.setVolumn(volumn);
        }
        else {
            p.setVolumn(p.getVolumn() + volumn);
        }
        var m = getProperMargin(c.getContractId(),
                            margin,
                            FeeStatus.DEALED);
        if (p.getMargin() == null) {
            p.setMargin(m);
        }
        else {
            p.setMargin(p.getMargin() + m);
        }
        var pprofit = getProperPositionProfit(c,
                                          price,
                                          instrument);
        if (p.getPositionProfit() == null) {
            p.setPositionProfit(pprofit);
        }
        else {
            p.setPositionProfit(p.getPositionProfit() + pprofit);
        }
    }

    private void addOpeningContract(Position p,
                                    Contract c,
                                    Collection<Commission> commissions,
                                    Margin margin) throws AlgorithmException {
        var frozenCommission = getProperCommission(c.getContractId(),
                                               commissions,
                                               FeeStatus.FORZEN);
        if (p.getFrozenCommission() == null) {
            p.setFrozenCommission(frozenCommission);
        }
        else {
            p.setFrozenCommission(p.getFrozenCommission() + frozenCommission);
        }
        var frozenOpenVolumn = getProperVolumn(c.getStatus(), ContractStatus.OPENING);
        if (p.getFrozenOpenVolumn() == null) {
            p.setFrozenOpenVolumn(frozenOpenVolumn);
        }
        else {
            p.setFrozenOpenVolumn(p.getFrozenOpenVolumn() + frozenOpenVolumn);
        }
        var frozenMargin = getProperMargin(c.getContractId(),
                                       margin,
                                       FeeStatus.FORZEN);
        if (p.getFrozenMargin() == null) {
            p.setFrozenMargin(frozenMargin);
        }
        else {
            p.setFrozenMargin(p.getFrozenMargin() + frozenMargin);
        }
    }

    private void addPreContract(Position p, Contract c, Margin margin) {
        if (p.getPreAmount() == null) {
            p.setPreAmount(c.getOpenAmount());
        }
        else {
            p.setPreAmount(p.getPreAmount() + c.getOpenAmount());
        }
        if (p.getPreVolumn() == null) {
            p.setPreVolumn(1L);
        }
        else {
            p.setPreVolumn(p.getPreVolumn() + 1L);
        }
        if (p.getPreMargin() == null) {
            p.setPreMargin(margin.getMargin());
        }
        else {
            p.setPreMargin(p.getPreMargin() + margin.getMargin());
        }
    }

    private void addPrePosition(Position p,
                                Contract c,
                                Collection<Commission> commissions,
                                Margin margin,
                                Double price,
                                Instrument instrument) throws AlgorithmException {
        var status = c.getStatus();
        if (status == null) {
            throw new AlgorithmException(ErrorCode.CONTRACT_STATUS_NULL.code(),
                                         ErrorCode.CONTRACT_STATUS_NULL.message()
                                         + "(Contract ID:" + c.getContractId() + ")");
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
                throw new AlgorithmException(ErrorCode.INVALID_CONTRACT_STATUS.code(),
                                             ErrorCode.INVALID_CONTRACT_STATUS.message()
                                             + "(Contract ID:" + c.getContractId() + ")");
        }
        addPreContract(p,
                       c,
                       margin);
    }

    private void addTodayContract(Position p,
                                  Contract c,
                                  Margin margin) throws AlgorithmException {
        if (p.getTodayAmount() == null) {
            p.setTodayAmount(c.getOpenAmount());
        }
        else {
            p.setTodayAmount(p.getTodayAmount() + c.getOpenAmount());
        }
        var volumn = getProperVolumn(c.getStatus(), ContractStatus.OPEN);
        if (p.getTodayVolumn() == null) {
            p.setTodayVolumn(volumn);
        }
        else {
            p.setTodayVolumn(p.getTodayVolumn() + volumn);
        }
        var m = getProperMargin(c.getContractId(),
                            margin,
                            FeeStatus.DEALED);
        if (p.getTodayMargin() == null) {
            p.setTodayMargin(m);
        }
        else {
            p.setTodayMargin(p.getTodayMargin() + m);
        }
    }

    private void addTodayOpenContract(Position p, Contract c, Margin margin) {
        if (p.getTodayOpenAmount() == null) {
            p.setTodayOpenAmount(c.getOpenAmount());
        }
        else {
            p.setTodayOpenAmount(p.getTodayOpenAmount() + c.getOpenAmount());
        }
        if (p.getTodayOpenVolumn() == null) {
            p.setTodayOpenVolumn(1L);
        }
        else {
            p.setTodayOpenVolumn(p.getTodayOpenVolumn() + 1L);
        }
        if (p.getTodayOpenMargin() == null) {
            p.setTodayOpenMargin(margin.getMargin());
        }
        else {
            p.setTodayOpenMargin(p.getTodayOpenMargin() + margin.getMargin());
        }
    }

    private void addTodayPosition(Position p,
                                  Contract c,
                                  Collection<Commission> commissions,
                                  Margin margin,
                                  Double price,
                                  Instrument instrument) throws AlgorithmException {
        var status = c.getStatus();
        if (status == null) {
            throw new AlgorithmException(ErrorCode.CONTRACT_STATUS_NULL.code(),
                                         ErrorCode.CONTRACT_STATUS_NULL.message()
                                         + "(Contract ID:" + c.getContractId() + ")");
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
                throw new AlgorithmException(ErrorCode.INVALID_CONTRACT_STATUS.code(),
                                             ErrorCode.INVALID_CONTRACT_STATUS.message()
                                             + "(Contract ID:" + c.getContractId() + ")");
        }
    }

    private void checkContractId(Long contractId) throws AlgorithmException {
        Objects.requireNonNull(contractId);
    }

    private void checkInstrument(Instrument instrument) throws AlgorithmException {
        Objects.requireNonNull(instrument);
    }

    private void checkInstrumentId(String instrumentId) throws AlgorithmException {
        Objects.requireNonNull(instrumentId);
        if (instrumentId.isBlank()) {
            throw new AlgorithmException(ErrorCode.INVALID_INSTRUMENT_ID.code(),
                                         ErrorCode.INVALID_INSTRUMENT_ID.message());
        }
    }

    private void checkPositionFieldNotNull(Object... values) throws AlgorithmException {
        for (var v : values) {
            if (v == null) {
                Objects.requireNonNull(v);
            }
        }
    }

    private void checkRatioType(Integer type) throws AlgorithmException {
        Objects.requireNonNull(type);
    }

    private Collection<Commission> findCommission(
            Long contractId,
            Map<Long, Set<Commission>> commissions) throws AlgorithmException {
        var v = commissions.get(contractId);
        if (v == null) {
            throw new AlgorithmException(ErrorCode.COMMISSION_NULL.code(),
                                         ErrorCode.COMMISSION_NULL.message()
                                         + "(Contract ID:" + contractId + ")");
        }
        return v;
    }

    private Instrument findInstrumentProperty(String key,
                                              Map<String, Instrument> instruments) throws AlgorithmException {
        var v = instruments.get(key);
        if (v == null) {
            throw new AlgorithmException(ErrorCode.INSTRUMENT_NULL.code(),
                                         ErrorCode.INSTRUMENT_NULL.message()
                                         + "(" + key + ")");
        }
        return v;
    }

    private Margin findMargin(Long contractId, Map<Long, Margin> margins) throws AlgorithmException {
        var v = margins.get(contractId);
        if (v == null) {
            throw new AlgorithmException(ErrorCode.MARGIN_NULL.code(),
                                         ErrorCode.MARGIN_NULL.message()
                                         + "(Contract ID:" + contractId + ")");
        }
        return v;
    }

    private double findPriceProperty(String key,
                                     Map<String, SettlementPrice> prices) throws AlgorithmException {
        var v = prices.get(key);
        if (v == null) {
            throw new AlgorithmException(ErrorCode.TICK_NULL.code(),
                                         ErrorCode.TICK_NULL.message()
                                         + "(" + key + ")");
        }
        var p = v.getSettlementPrice();
        if (p == null) {
            throw new AlgorithmException(ErrorCode.PRICE_NULL.code(),
                                         ErrorCode.PRICE_NULL.message()
                                         + "(" + key + ")");
        }
        return p;
    }

    private void setOrderStatus(Order order) throws AlgorithmException {
        if (order.getStatus() != null) {
            return;
        }
        var traded = order.getTradedVolumn();
        if (traded > order.getQuantity()) {
            throw new AlgorithmException(ErrorCode.INCONSISTENT_CONTRACT_ORDER_INFO.code(),
                                         ErrorCode.INCONSISTENT_CONTRACT_ORDER_INFO.message()
                                         + "(Order ID:" + order.getOrderId() + ")");
        }
        if (traded > 0) {
            if (Objects.equals(traded, order.getQuantity())) {
                order.setStatus(OrderStatus.ALL_TRADED);
            }
            else {
                order.setStatus(OrderStatus.QUEUED);
            }
        }
        else {
            order.setStatus(OrderStatus.ACCEPTED);
        }
    }

    private double getProperCommission(Long contractId,
                                       Collection<Commission> commissions,
                                       Integer status) throws AlgorithmException {
        double v = 0D;
        for (var c : commissions) {
            if (Objects.equals(c.getContractId(), contractId)
                && Objects.equals(c.getStatus(), status)) {
                var x = c.getCommission();
                if (x == null) {
                    throw new AlgorithmException(ErrorCode.COMMISSION_AMOUNT_NULL.code(),
                                                 ErrorCode.COMMISSION_AMOUNT_NULL.message());
                }
                v += x;
            }
        }
        return v;
    }

    private Double getProperCommissionRatio(Instrument instrument,
                                            Integer offset) throws AlgorithmException {
        checkInstrument(instrument);
        if (offset == Offset.OPEN) {
            return instrument.getCommissionOpenRatio();
        }
        else {
            if (offset == Offset.CLOSE) {
                return instrument.getCommissionCloseRatio();
            }
            else {
                return instrument.getCommissionCloseTodayRatio();
            }
        }
    }

    private double getProperDeposit(Collection<Deposit> deposits) throws AlgorithmException {
        if (deposits == null) {
            throw new AlgorithmException(ErrorCode.DEPOSIT_NULL.code(),
                                         ErrorCode.DEPOSIT_NULL.message());
        }
        double deposit = 0D;
        for (var d : deposits) {
            var a = d.getAmount();
            if (a == null) {
                throw new AlgorithmException(ErrorCode.DEPOSIT_AMOUNT_NULL.code(),
                                             ErrorCode.DEPOSIT_AMOUNT_NULL.message());
            }
            deposit += d.getAmount();
        }
        return deposit;
    }

    private double getProperMargin(Long contractId,
                                   Margin margin,
                                   Integer status) throws AlgorithmException {
        if (Objects.equals(contractId, margin.getContractId())
            && Objects.equals(margin.getStatus(), status)) {
            return margin.getMargin();
        }
        else {
            throw new AlgorithmException(ErrorCode.INVALID_FEE_STATUS.code(),
                                         ErrorCode.INVALID_FEE_STATUS.message());
        }
    }

    private double getProperPositionProfit(Contract c,
                                           Double price,
                                           Instrument instrument) throws AlgorithmException {
        var a = getAmount(price, instrument);
        return getProperProfit(c.getOpenAmount(),
                               a,
                               c.getDirection());
    }

    private double getProperProfit(double pre,
                                   double current,
                                   Integer direction) throws AlgorithmException {
        if (direction == null) {
            throw new AlgorithmException(ErrorCode.DIRECTION_NULL.code(),
                                         ErrorCode.DIRECTION_NULL.message());
        }
        if (direction == Direction.BUY) {
            return current - pre;
        }
        else {
            return pre - current;
        }
    }

    private long getProperVolumn(Integer status,
                                 Integer wantedStatus) throws AlgorithmException {
        if (Objects.equals(status, wantedStatus)) {
            return 1L;
        }
        else {
            throw new AlgorithmException(ErrorCode.INVALID_CONTRACT_STATUS.code(),
                                         ErrorCode.INVALID_CONTRACT_STATUS.message());
        }
    }

    private double getProperWithdraw(Collection<Withdraw> withdraws) throws AlgorithmException {
        if (withdraws == null) {
            throw new AlgorithmException(ErrorCode.WITHDRAW_NULL.code(),
                                         ErrorCode.DEPOSIT_NULL.message());
        }
        double withdraw = 0D;
        for (var w : withdraws) {
            var a = w.getAmount();
            if (a == null) {
                throw new AlgorithmException(ErrorCode.WITHDRAW_AMOUNT_NULL.code(),
                                             ErrorCode.WITHDRAW_AMOUNT_NULL.message());
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

    private void setContracts(Order order,
                              Collection<Contract> contracts) throws AlgorithmException {
        double amount = 0D;
        long tradedVolumn = 0L;
        for (var c : contracts) {
            if (!Objects.equals(c.getDirection(), order.getDirection())
                || c.getInstrumentId().equals(order.getInstrumentId())) {
                throw new AlgorithmException(ErrorCode.INCONSISTENT_CONTRACT_ORDER_INFO.code(),
                                             ErrorCode.INCONSISTENT_CONTRACT_ORDER_INFO.message());
            }
            amount += c.getOpenAmount();
            ++tradedVolumn;
        }
        order.setAmount(amount);
        order.setTradedVolumn(tradedVolumn);
    }

    private void setDeleted(Order order,
                            Collection<Response> responses) throws AlgorithmException {
        if (responses == null) {
            throw new AlgorithmException(ErrorCode.NO_RESPONSE.code(),
                                         ErrorCode.NO_RESPONSE.message());
        }
        if (responses.isEmpty()) {
            order.setDeleted(Boolean.FALSE);
        }
        else {
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
                           Collection<Trade> trades) throws AlgorithmException {
        if (trades == null) {
            throw new AlgorithmException(ErrorCode.NO_TRADE.code(),
                                         ErrorCode.NO_TRADE.message());
        }
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