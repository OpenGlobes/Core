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

import com.openglobes.core.Account;
import com.openglobes.core.Commission;
import com.openglobes.core.Contract;
import com.openglobes.core.ContractStatus;
import com.openglobes.core.Deposit;
import com.openglobes.core.Direction;
import com.openglobes.core.FeeStatus;
import com.openglobes.core.Instrument;
import com.openglobes.core.Margin;
import com.openglobes.core.Offset;
import com.openglobes.core.Order;
import com.openglobes.core.OrderStatus;
import com.openglobes.core.Position;
import com.openglobes.core.RatioType;
import com.openglobes.core.Request;
import com.openglobes.core.Response;
import com.openglobes.core.Tick;
import com.openglobes.core.Trade;
import com.openglobes.core.Withdraw;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Algorithm implemetation.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class TraderEngineAlgorithm implements ITraderEngineAlgorithm {

    public TraderEngineAlgorithm() {
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
            throw new AlgorithmException(ExceptionCodes.POSITION_NULL.code(),
                                               ExceptionCodes.POSITION_NULL.message());
        }
        for (var p : positions) {
            check4(p.getCloseProfit(),
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
        check0(instrument);
        var multiple = instrument.getMultiple();
        if (multiple == null) {
            throw new AlgorithmException(ExceptionCodes.MULTIPLE_NULL.code(),
                                               ExceptionCodes.MULTIPLE_NULL.message()
                                               + "(" + instrument.getInstrumentId() + ")");
        }
        return price * multiple;
    }

    @Override
    public double getCommission(double price,
                                Instrument instrument,
                                Direction direction,
                                Offset offset) throws AlgorithmException {
        check0(instrument);
        var ctype = instrument.getCommissionType();
        check1(ctype);
        var ratio = getProperCommissionRatio(instrument, offset);
        if (ratio == null) {
            throw new AlgorithmException(ExceptionCodes.RATIO_NULL.code(),
                                               ExceptionCodes.RATIO_NULL.message()
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
        check0(instrument);
        var type = instrument.getMarginType();
        check1(type);
        var ratio = instrument.getMarginRatio();
        if (ratio == null) {
            throw new AlgorithmException(ExceptionCodes.RATIO_NULL.code(),
                                               ExceptionCodes.RATIO_NULL.message()
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
                                             Map<String, Tick> ticks,
                                             Map<String, Instrument> instruments,
                                             LocalDate tradingDay) throws AlgorithmException {
        if (contracts == null) {
            throw new AlgorithmException(ExceptionCodes.CONTRACT_NULL.code(),
                                               ExceptionCodes.CONTRACT_NULL.message());
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
                throw new AlgorithmException(ExceptionCodes.DIRECTION_NULL.code(),
                                                   ExceptionCodes.DIRECTION_NULL.message());
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
            check2(iid);
            var cid = c.getContractId();
            check3(cid);
            var pk = iid + ".Price";
            var ik = iid + ".Instrument";
            var margin = findMargin(cid, map);
            var commission = findCommission(cid, cmap);
            var price = findPriceProperty(pk, ticks);
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
            throw new AlgorithmException(ExceptionCodes.CONTRACT_STATUS_NULL.code(),
                                               ExceptionCodes.CONTRACT_STATUS_NULL.message()
                                               + "(Contract ID:" + c.getContractId() + ")");
        }
        switch (status) {
            case CLOSED:
                addClosedContract(p,
                                  c,
                                  commissions);
                break;
            case CLOSING:
                addClosingContract(p,
                                   c,
                                   commissions,
                                   margin,
                                   price,
                                   instrument);
                break;
            case OPEN:
                addOpenContract(p,
                                c,
                                commissions,
                                margin,
                                price,
                                instrument);
                break;
            default:
                throw new AlgorithmException(ExceptionCodes.INVALID_CONTRACT_STATUS.code(),
                                                   ExceptionCodes.INVALID_CONTRACT_STATUS.message()
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
            throw new AlgorithmException(ExceptionCodes.CONTRACT_STATUS_NULL.code(),
                                               ExceptionCodes.CONTRACT_STATUS_NULL.message()
                                               + "(Contract ID:" + c.getContractId() + ")");
        }
        switch (status) {
            case CLOSED:
                addClosedContract(p,
                                  c,
                                  commissions);
                addTodayOpenContract(p,
                                     c,
                                     margin);
                break;
            case OPENING:
                addOpeningContract(p,
                                   c,
                                   commissions,
                                   margin);
                break;
            case CLOSING:
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
            default: // OPEN
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
        }
    }

    private void check0(Instrument instrument) throws AlgorithmException {
        if (instrument == null) {
            throw new AlgorithmException(ExceptionCodes.INSTRUMENT_NULL.code(),
                                               ExceptionCodes.INSTRUMENT_NULL.message());
        }
    }

    private void check1(RatioType type) throws AlgorithmException {
        if (type == null) {
            throw new AlgorithmException(ExceptionCodes.RATIO_TYPE_NULL.code(),
                                               ExceptionCodes.RATIO_TYPE_NULL.message());
        }
    }

    private void check2(String instrumentId) throws AlgorithmException {
        if (instrumentId == null) {
            throw new AlgorithmException(ExceptionCodes.INSTRUMENT_ID_NULL.code(),
                                               ExceptionCodes.INSTRUMENT_ID_NULL.message());
        }
        if (instrumentId.isBlank()) {
            throw new AlgorithmException(ExceptionCodes.INVALID_INSTRUMENT_ID.code(),
                                               ExceptionCodes.INVALID_INSTRUMENT_ID.message());
        }
    }

    private void check3(Long contractId) throws AlgorithmException {
        if (contractId == null) {
            throw new AlgorithmException(ExceptionCodes.CONTRACT_ID_NULL.code(),
                                               ExceptionCodes.CONTRACT_ID_NULL.message());
        }
    }

    private void check4(Object... values) throws AlgorithmException {
        for (var v : values) {
            if (v == null) {
                throw new AlgorithmException(ExceptionCodes.POSITION_FIELD_NULL.code(),
                                                   ExceptionCodes.POSITION_FIELD_NULL.message());
            }
        }
    }

    private Collection<Commission> findCommission(
            Long contractId,
            Map<Long, Set<Commission>> commissions) throws AlgorithmException {
        var v = commissions.get(contractId);
        if (v == null) {
            throw new AlgorithmException(ExceptionCodes.COMMISSION_NULL.code(),
                                               ExceptionCodes.COMMISSION_NULL.message()
                                               + "(Contract ID:" + contractId + ")");
        }
        return v;
    }

    private Instrument findInstrumentProperty(String key,
                                              Map<String, Instrument> instruments) throws AlgorithmException {
        var v = instruments.get(key);
        if (v == null) {
            throw new AlgorithmException(ExceptionCodes.INSTRUMENT_NULL.code(),
                                               ExceptionCodes.INSTRUMENT_NULL.message()
                                               + "(" + key + ")");
        }
        return v;
    }

    private Margin findMargin(Long contractId, Map<Long, Margin> margins) throws AlgorithmException {
        var v = margins.get(contractId);
        if (v == null) {
            throw new AlgorithmException(ExceptionCodes.MARGIN_NULL.code(),
                                               ExceptionCodes.MARGIN_NULL.message()
                                               + "(Contract ID:" + contractId + ")");
        }
        return v;
    }

    private double findPriceProperty(String key,
                                     Map<String, Tick> ticks) throws AlgorithmException {
        var v = ticks.get(key);
        if (v == null) {
            throw new AlgorithmException(ExceptionCodes.TICK_NULL.code(),
                                               ExceptionCodes.TICK_NULL.message()
                                               + "(" + key + ")");
        }
        var p = v.getPrice();
        if (p == null) {
            throw new AlgorithmException(ExceptionCodes.PRICE_NULL.code(),
                                               ExceptionCodes.PRICE_NULL.message()
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
            throw new AlgorithmException(ExceptionCodes.INCONSISTENT_CONTRACT_ORDER_INFO.code(),
                                               ExceptionCodes.INCONSISTENT_CONTRACT_ORDER_INFO.message()
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
                                       FeeStatus status) throws AlgorithmException {
        double v = 0D;
        for (var c : commissions) {
            if (Objects.equals(c.getContractId(), contractId) && c.getStatus() == status) {
                var x = c.getCommission();
                if (x == null) {
                    throw new AlgorithmException(ExceptionCodes.COMMISSION_AMOUNT_NULL.code(),
                                                       ExceptionCodes.COMMISSION_AMOUNT_NULL.message());
                }
                v += x;
            }
        }
        return v;
    }

    private Double getProperCommissionRatio(Instrument instrument,
                                            Offset offset) throws AlgorithmException {
        check0(instrument);
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
            throw new AlgorithmException(ExceptionCodes.DEPOSIT_NULL.code(),
                                               ExceptionCodes.DEPOSIT_NULL.message());
        }
        double deposit = 0D;
        for (var d : deposits) {
            var a = d.getAmount();
            if (a == null) {
                throw new AlgorithmException(ExceptionCodes.DEPOSIT_AMOUNT_NULL.code(),
                                                   ExceptionCodes.DEPOSIT_AMOUNT_NULL.message());
            }
            deposit += d.getAmount();
        }
        return deposit;
    }

    private double getProperMargin(Long contractId,
                                   Margin margin,
                                   FeeStatus status) throws AlgorithmException {
        if (Objects.equals(contractId, margin.getContractId())
            && margin.getStatus() == status) {
            return margin.getMargin();
        }
        else {
            throw new AlgorithmException(ExceptionCodes.INVALID_FEE_STATUS.code(),
                                               ExceptionCodes.INVALID_FEE_STATUS.message());
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
                                   Direction direction) throws AlgorithmException {
        if (direction == null) {
            throw new AlgorithmException(ExceptionCodes.DIRECTION_NULL.code(),
                                               ExceptionCodes.DIRECTION_NULL.message());
        }
        if (direction == Direction.BUY) {
            return current - pre;
        }
        else {
            return pre - current;
        }
    }

    private long getProperVolumn(ContractStatus status,
                                 ContractStatus wantedStatus) throws AlgorithmException {
        if (status == wantedStatus) {
            return 1L;
        }
        else {
            throw new AlgorithmException(ExceptionCodes.INVALID_CONTRACT_STATUS.code(),
                                               ExceptionCodes.INVALID_CONTRACT_STATUS.message());
        }
    }

    private double getProperWithdraw(Collection<Withdraw> withdraws) throws AlgorithmException {
        if (withdraws == null) {
            throw new AlgorithmException(ExceptionCodes.WITHDRAW_NULL.code(),
                                               ExceptionCodes.DEPOSIT_NULL.message());
        }
        double withdraw = 0D;
        for (var w : withdraws) {
            var a = w.getAmount();
            if (a == null) {
                throw new AlgorithmException(ExceptionCodes.WITHDRAW_AMOUNT_NULL.code(),
                                                   ExceptionCodes.WITHDRAW_AMOUNT_NULL.message());
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
            if (c.getDirection() != order.getDirection()
                || c.getInstrumentId().equals(order.getInstrumentId())) {
                throw new AlgorithmException(ExceptionCodes.INCONSISTENT_CONTRACT_ORDER_INFO.code(),
                                                   ExceptionCodes.INCONSISTENT_CONTRACT_ORDER_INFO.message());
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
            throw new AlgorithmException(ExceptionCodes.NO_RESPONSE.code(),
                                               ExceptionCodes.NO_RESPONSE.message());
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
            throw new AlgorithmException(ExceptionCodes.NO_TRADE.code(),
                                               ExceptionCodes.NO_TRADE.message());
        }
        if (trades.isEmpty()) {
            return;
        }
        var responses = new LinkedList<Trade>(trades);
        responses.sort((Trade o1, Trade o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
        order.setTradingDay(responses.getFirst().getTradingDay());
        order.setInsertTimestamp(responses.getFirst().getTimestamp());
        order.setUpdateTimestamp(responses.getLast().getTimestamp());
    }
}
