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
package com.openglobes.core.data;

import com.openglobes.core.dba.DbaException;
import com.openglobes.core.dba.ICondition;
import com.openglobes.core.dba.IDefaultFactory;
import com.openglobes.core.dba.IQuery;
import com.openglobes.core.dba.Queries;
import com.openglobes.core.event.EventSourceException;
import com.openglobes.core.trader.Account;
import com.openglobes.core.trader.Commission;
import com.openglobes.core.trader.Contract;
import com.openglobes.core.trader.Deposit;
import com.openglobes.core.trader.ErrorCode;
import com.openglobes.core.trader.Instrument;
import com.openglobes.core.trader.Margin;
import com.openglobes.core.trader.Request;
import com.openglobes.core.trader.Response;
import com.openglobes.core.trader.SettlementPrice;
import com.openglobes.core.trader.Trade;
import com.openglobes.core.trader.TradingDay;
import com.openglobes.core.trader.Withdraw;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Collection;

/**
 * Trader engine's data connection.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class DefaultTraderDataConnection extends TraderDataConnection {

    private final IQuery query;

    public DefaultTraderDataConnection(Connection connection, 
                                       DefaultTraderDataSource source) throws DataSourceException {
        super(connection, source);
        query = Queries.createQuery(conn());
    }

    @Override
    public void addAccount(Account account) throws DataSourceException {
        callInsert(Account.class, account);
    }

    @Override
    public void addCommission(Commission commission) throws DataSourceException {
        callInsert(Commission.class, commission);
    }

    @Override
    public void addContract(Contract contract) throws DataSourceException {
        callInsert(Contract.class, contract);
    }

    @Override
    public void addDeposit(Deposit deposit) throws DataSourceException {
        callInsert(Deposit.class, deposit);
    }

    @Override
    public void addInstrument(Instrument instrument) throws DataSourceException {
        callInsert(Instrument.class, instrument);
    }

    @Override
    public void addMargin(Margin margin) throws DataSourceException {
        callInsert(Margin.class, margin);
    }

    @Override
    public void addRequest(Request request) throws DataSourceException {
        callInsert(Request.class, request);
    }

    @Override
    public void addResponse(Response response) throws DataSourceException {
        callInsert(Response.class, response);
    }

    @Override
    public void addSettlementPrice(SettlementPrice price) throws DataSourceException {
        callInsert(SettlementPrice.class, price);
    }

    @Override
    public void addTrade(Trade trade) throws DataSourceException {
        callInsert(Trade.class, trade);
    }

    @Override
    public void addTradingDay(TradingDay day) throws DataSourceException {
        callInsert(TradingDay.class, day);
    }

    @Override
    public void addWithdraw(Withdraw withdraw) throws DataSourceException {
        callInsert(Withdraw.class, withdraw);
    }

    @Override
    public Account getAccount() throws DataSourceException {
        try {
            return callGetSingle(Account.class,
                                 Queries.isNotNull(Account.class.getDeclaredField("accountId")),
                                 Account::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Commission getCommissionById(Long commissionId) throws DataSourceException {
        try {
            return callGetSingle(Commission.class,
                                 Queries.equals(Commission.class.getDeclaredField("commissionId"), commissionId),
                                 Commission::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Commission> getCommissions() throws DataSourceException {
        try {
            return callGetMany(Commission.class,
                               Queries.isNotNull(Commission.class.getDeclaredField("commissionId")),
                               Commission::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Commission> getCommissionsByOrderId(long orderId) throws DataSourceException {
        try {
            return callGetMany(Commission.class,
                               Queries.equals(Commission.class.getDeclaredField("orderId"), orderId),
                               Commission::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Commission> getCommissionsByStatus(Integer status) throws DataSourceException {
        try {
            return callGetMany(Commission.class,
                               Queries.equals(Commission.class.getDeclaredField("status"), status),
                               Commission::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Contract getContractById(Long contractId) throws DataSourceException {
        try {
            return callGetSingle(Contract.class,
                                 Queries.equals(Contract.class.getDeclaredField("contractId"), contractId),
                                 Contract::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Contract> getContracts() throws DataSourceException {
        try {
            return callGetMany(Contract.class,
                               Queries.isNotNull(Contract.class.getDeclaredField("contractId")),
                               Contract::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Contract> getContractsByInstrumentId(String instrumentId) throws DataSourceException {
        try {
            return callGetMany(Contract.class,
                               Queries.equals(Contract.class.getDeclaredField("instrumentId"), instrumentId),
                               Contract::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Contract> getContractsByStatus(Integer status) throws DataSourceException {
        try {
            return callGetMany(Contract.class,
                               Queries.equals(Contract.class.getDeclaredField("status"), status),
                               Contract::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Contract> getContractsByTradeId(long tradeId) throws DataSourceException {
        try {
            return callGetMany(Contract.class,
                               Queries.equals(Contract.class.getDeclaredField("tradeId"), tradeId),
                               Contract::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public ITraderDataSource getDataSource() throws DataSourceException {
        var r = super.getSource();
        if (r instanceof ITraderDataSource) {
            return (ITraderDataSource) r;
        }
        else {
            throw new DataSourceException(ErrorCode.INVALID_DATASOURCE_TYPE.code(),
                                          ErrorCode.INVALID_DATASOURCE_TYPE.message());
        }
    }

    @Override
    public Collection<Deposit> getDeposits() throws DataSourceException {
        try {
            return callGetMany(Deposit.class,
                               Queries.isNotNull(Deposit.class.getDeclaredField("depositId")),
                               Deposit::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Instrument getInstrumentById(String instrumentId) throws DataSourceException {
        try {
            return callGetSingle(Instrument.class,
                                 Queries.equals(Instrument.class.getDeclaredField("instrumentId"), instrumentId),
                                 Instrument::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Instrument> getInstrumentsByExchangeId(String exchangeId) throws DataSourceException {
        try {
            return callGetMany(Instrument.class,
                               Queries.equals(Instrument.class.getDeclaredField("exchangeId"), exchangeId),
                               Instrument::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Margin getMarginById(Long marginId) throws DataSourceException {
        try {
            return callGetSingle(Margin.class,
                                 Queries.equals(Margin.class.getDeclaredField("marginId"), marginId),
                                 Margin::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Margin> getMargins() throws DataSourceException {
        try {
            return callGetMany(Margin.class,
                               Queries.isNotNull(Margin.class.getDeclaredField("marginId")),
                               Margin::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Margin> getMarginsByOrderId(long orderId) throws DataSourceException {
        try {
            return callGetMany(Margin.class,
                               Queries.equals(Margin.class.getDeclaredField("orderId"), orderId),
                               Margin::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Margin> getMarginsByStatus(Integer status) throws DataSourceException {
        try {
            return callGetMany(Margin.class,
                               Queries.equals(Margin.class.getDeclaredField("status"), status),
                               Margin::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Request getRequestByOrderId(long orderId) throws DataSourceException {
        try {
            return callGetSingle(Request.class,
                                 Queries.equals(Request.class.getDeclaredField("orderId"), orderId),
                                 Request::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Request> getRequests() throws DataSourceException {
        try {
            return callGetMany(Request.class,
                               Queries.isNotNull(Request.class.getDeclaredField("orderId")),
                               Request::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Response getResponseById(long responseId) throws DataSourceException {
        try {
            return callGetSingle(Response.class,
                                 Queries.equals(Response.class.getDeclaredField("responseId"), responseId),
                                 Response::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Response> getResponseByOrderId(long orderId) throws DataSourceException {
        try {
            return callGetMany(Response.class,
                               Queries.equals(Response.class.getDeclaredField("orderId"), orderId),
                               Response::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Response> getResponses() throws DataSourceException {
        try {
            return callGetMany(Response.class,
                               Queries.isNotNull(Response.class.getDeclaredField("responseId")),
                               Response::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public SettlementPrice getSettlementPriceByInstrumentId(String instrumentId) throws DataSourceException {
        try {
            return callGetSingle(SettlementPrice.class,
                                 Queries.equals(SettlementPrice.class.getDeclaredField("instrumentId"), instrumentId),
                                 SettlementPrice::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Connection getSqlConnection() throws DataSourceException {
        return conn();
    }

    @Override
    public Trade getTradeById(Long tradeId) throws DataSourceException {
        try {
            return callGetSingle(Trade.class,
                                 Queries.equals(Trade.class.getDeclaredField("tradeId"), tradeId),
                                 Trade::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Trade> getTrades() throws DataSourceException {
        try {
            return callGetMany(Trade.class,
                               Queries.isNotNull(Trade.class.getDeclaredField("tradeId")),
                               Trade::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Trade> getTradesByOrderId(long orderId) throws DataSourceException {
        try {
            return callGetMany(Trade.class,
                               Queries.equals(Trade.class.getDeclaredField("orderId"), orderId),
                               Trade::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public TradingDay getTradingDay() throws DataSourceException {
        try {
            return callGetSingle(TradingDay.class,
                                 Queries.isNotNull(TradingDay.class.getDeclaredField("tradingDayId")),
                                 TradingDay::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public Collection<Withdraw> getWithdraws() throws DataSourceException {
        try {
            return callGetMany(Withdraw.class,
                               Queries.isNotNull(Withdraw.class.getDeclaredField("withdrawId")),
                               Withdraw::new);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public void removeCommission(long commissionId) throws DataSourceException {
        callRemove(Commission.class,
                   "commissionId",
                   commissionId,
                   Commission::new);
    }

    @Override
    public void removeContract(long contractId) throws DataSourceException {
        callRemove(Contract.class,
                   "contractId",
                   contractId,
                   Contract::new);
    }

    @Override
    public void removeDeposit(long depositId) throws DataSourceException {
        callRemove(Deposit.class,
                   "depositId",
                   depositId,
                   Deposit::new);
    }

    @Override
    public void removeInstrument(String instrumentId) throws DataSourceException {
        callRemove(Instrument.class,
                   "instrumentId",
                   instrumentId,
                   Instrument::new);
    }

    @Override
    public void removeMargin(long marginId) throws DataSourceException {
        callRemove(Margin.class,
                   "marginId",
                   marginId,
                   Margin::new);
    }

    @Override
    public void removeSettlementPrice(String instrumentId) throws DataSourceException {
        callRemove(SettlementPrice.class,
                   "instrumentId",
                   instrumentId,
                   SettlementPrice::new);
    }

    @Override
    public void removeWithdraw(long withdrawId) throws DataSourceException {
        callRemove(Withdraw.class,
                   "withdrawId",
                   withdrawId,
                   Withdraw::new);
    }



    @Override
    public void updateAccount(Account account) throws DataSourceException {
        try {
            callUpdate(Account.class,
                       account,
                       Account.class.getDeclaredField("accountId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public void updateCommission(Commission commission) throws DataSourceException {
        try {
            callUpdate(Commission.class,
                       commission,
                       Commission.class.getDeclaredField("commissionId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public void updateContract(Contract contract) throws DataSourceException {
        try {
            callUpdate(Contract.class,
                       contract,
                       Contract.class.getDeclaredField("contractId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public void updateInstrument(Instrument instrument) throws DataSourceException {
        try {
            callUpdate(Instrument.class,
                       instrument,
                       Instrument.class.getDeclaredField("instrumentId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public void updateMargin(Margin margin) throws DataSourceException {
        try {
            callUpdate(Margin.class,
                       margin,
                       Margin.class.getDeclaredField("marginId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public void updateSettlementPrice(SettlementPrice price) throws DataSourceException {
        try {
            callUpdate(SettlementPrice.class,
                       price,
                       SettlementPrice.class.getDeclaredField("instrumentId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public void updateTradingDay(TradingDay day) throws DataSourceException {
        try {
            callUpdate(TradingDay.class,
                       day,
                       TradingDay.class.getDeclaredField("tradingDayId"));
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
    }

    private <T> Collection<T> callGetMany(Class<T> clazz,
                                          ICondition<?> condition,
                                          IDefaultFactory<T> factory) throws DataSourceException {
        try {
            return query.select(clazz, condition, factory);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.DBA_SELECT_FAIL.code(),
                                          ErrorCode.DBA_SELECT_FAIL.message() + " " + clazz.getCanonicalName(),
                                          ex);
        }
    }

    private <T> T callGetSingle(Class<T> clazz,
                                ICondition<?> condition,
                                IDefaultFactory<T> factory) throws DataSourceException {
        var c = callGetMany(clazz, condition, factory);
        if (c.size() > 1) {
            throw new DataSourceException(
                    ErrorCode.MORE_ROWS_THAN_EXPECTED.code(),
                    ErrorCode.MORE_ROWS_THAN_EXPECTED.message() + " " + clazz.getCanonicalName());
        }
        if (c.isEmpty()) {
            throw new DataSourceException(
                    ErrorCode.LESS_ROWS_THAN_EXPECTED.code(),
                    ErrorCode.LESS_ROWS_THAN_EXPECTED.message() + " " + clazz.getCanonicalName());
        }
        return c.iterator().next();
    }

    private <T> void callInsert(Class<T> clazz, T object) throws DataSourceException {
        try {
            query.insert(clazz, object);
            callOnChange(clazz,
                         object,
                         DataChangeType.CREATE);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.DBA_INSERT_FAIL.code(),
                                          ErrorCode.DBA_INSERT_FAIL.message(),
                                          ex);
        }
    }

    private <T> void callOnChange(Class<T> clazz,
                                  T object,
                                  DataChangeType type) throws DataSourceException {
        try {
            getDataSource().getEventSource(type).publish(clazz, object);
        }
        catch (EventSourceException ex) {
            throw new DataSourceException(ErrorCode.PUBLISH_EVENT_FAIL.code(),
                                          ex.getMessage(),
                                          ex);
        }
    }

    private <T, V> void callRemove(Class<T> clazz,
                                   String fieldName,
                                   V id,
                                   IDefaultFactory<T> factory) throws DataSourceException {
        try {
            callOnChange(clazz,
                         callGetSingle(clazz,
                                       Queries.equals(clazz.getDeclaredField(fieldName), id),
                                       factory),
                         DataChangeType.DELETE);
            query.remove(clazz,
                         Queries.equals(clazz.getField(fieldName), id));
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.OBTAIN_CONDITION_FAIL.code(),
                                          ErrorCode.OBTAIN_CONDITION_FAIL.message(),
                                          ex);
        }
        catch (NoSuchFieldException | SecurityException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
    }

    private <T> void callUpdate(Class<T> clazz, 
                                T object,
                                Field field) throws DataSourceException {
        try {
            query.update(clazz,
                         object,
                         Queries.equals(field, field.getLong(object)));
            callOnChange(clazz,
                         object,
                         DataChangeType.UPDATE);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.DBA_UPDATE_FAIL.code(),
                                          ErrorCode.DBA_UPDATE_FAIL.message() + " " + clazz.getCanonicalName(),
                                          ex);
        }
        catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new DataSourceException(ErrorCode.REFLECTION_FAIL.code(),
                                          ErrorCode.REFLECTION_FAIL.message(),
                                          ex);
        }
    }
}
