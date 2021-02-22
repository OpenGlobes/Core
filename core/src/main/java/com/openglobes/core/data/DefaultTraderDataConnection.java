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

import com.openglobes.core.dba.*;
import com.openglobes.core.event.EventException;
import com.openglobes.core.trader.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Trader engine's data connection.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class DefaultTraderDataConnection extends AbstractTraderDataConnection {

    private final IQuery query;

    public DefaultTraderDataConnection(Connection connection,
                                       DefaultTraderDataSource source) {
        super(connection, source);
        query = Queries.createQuery(conn());
    }

    @Override
    public void addAccount(Account account) throws DataInsertionException {
        try {
            callInsert(Account.class, account);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataInsertionException(ex.getMessage(),
                                             ex);
        }
    }

    @Override
    public void addCommission(Commission commission) throws DataInsertionException {
        try {
            callInsert(Commission.class, commission);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataInsertionException(ex.getMessage(),
                                             ex);
        }
    }

    @Override
    public void addContract(Contract contract) throws DataInsertionException {
        try {
            callInsert(Contract.class, contract);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataInsertionException(ex.getMessage(),
                                             ex);
        }
    }

    @Override
    public void addDeposit(Deposit deposit) throws DataInsertionException {
        try {
            callInsert(Deposit.class, deposit);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataInsertionException(ex.getMessage(),
                                             ex);
        }
    }

    @Override
    public void addInstrument(Instrument instrument) throws DataInsertionException {
        try {
            callInsert(Instrument.class, instrument);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataInsertionException(ex.getMessage(),
                                             ex);
        }
    }

    @Override
    public void addMargin(Margin margin) throws DataInsertionException {
        try {
            callInsert(Margin.class, margin);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataInsertionException(ex.getMessage(),
                                             ex);
        }
    }

    @Override
    public void addRequest(Request request) throws DataInsertionException {
        try {
            callInsert(Request.class, request);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataInsertionException(ex.getMessage(),
                                             ex);
        }
    }

    @Override
    public void addResponse(Response response) throws DataInsertionException {
        try {
            callInsert(Response.class, response);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataInsertionException(ex.getMessage(),
                                             ex);
        }
    }

    @Override
    public void addSettlementPrice(SettlementPrice price) throws DataInsertionException {
        try {
            callInsert(SettlementPrice.class, price);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataInsertionException(ex.getMessage(),
                                             ex);
        }
    }

    @Override
    public void addTrade(Trade trade) throws DataInsertionException {
        try {
            callInsert(Trade.class, trade);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataInsertionException(ex.getMessage(),
                                             ex);
        }
    }

    @Override
    public void addTradingDay(TradingDay day) throws DataInsertionException {
        try {
            callInsert(TradingDay.class, day);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataInsertionException(ex.getMessage(),
                                             ex);
        }
    }

    @Override
    public void addWithdraw(Withdraw withdraw) throws DataInsertionException {
        try {
            callInsert(Withdraw.class, withdraw);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataInsertionException(ex.getMessage(),
                                             ex);
        }
    }

    @Override
    public Account getAccount() throws DataQueryException {
        try {
            return callGetSingle(Account.class,
                                 Queries.isNotNull(Account.class.getDeclaredField("accountId")),
                                 Account::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Account.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Commission getCommissionById(Long commissionId) throws DataQueryException {
        try {
            return callGetSingle(Commission.class,
                                 Queries.equals(Commission.class.getDeclaredField("commissionId"), commissionId),
                                 Commission::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Commission.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Commission> getCommissions() throws DataQueryException {
        try {
            return callGetMany(Commission.class,
                               Queries.isNotNull(Commission.class.getDeclaredField("commissionId")),
                               Commission::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Commission.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Commission> getCommissionsByOrderId(long orderId) throws DataQueryException {
        try {
            return callGetMany(Commission.class,
                               Queries.equals(Commission.class.getDeclaredField("orderId"), orderId),
                               Commission::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Commission.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Commission> getCommissionsByStatus(Integer status) throws DataQueryException {
        try {
            return callGetMany(Commission.class,
                               Queries.equals(Commission.class.getDeclaredField("status"), status),
                               Commission::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Commission.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Contract getContractById(Long contractId) throws DataQueryException {
        try {
            return callGetSingle(Contract.class,
                                 Queries.equals(Contract.class.getDeclaredField("contractId"), contractId),
                                 Contract::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Contract.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Contract> getContracts() throws DataQueryException {
        try {
            return callGetMany(Contract.class,
                               Queries.isNotNull(Contract.class.getDeclaredField("contractId")),
                               Contract::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Contract.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Contract> getContractsByInstrumentId(String instrumentId) throws DataQueryException {
        try {
            return callGetMany(Contract.class,
                               Queries.equals(Contract.class.getDeclaredField("instrumentId"), instrumentId),
                               Contract::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Contract.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Contract> getContractsByStatus(Integer status) throws DataQueryException {
        try {
            return callGetMany(Contract.class,
                               Queries.equals(Contract.class.getDeclaredField("status"), status),
                               Contract::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Contract.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Contract> getContractsByTradeId(long tradeId) throws DataQueryException {
        try {
            return callGetMany(Contract.class,
                               Queries.equals(Contract.class.getDeclaredField("tradeId"), tradeId),
                               Contract::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Contract.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public ITraderDataSource getDataSource() {
        return (ITraderDataSource) super.getSource();
    }

    @Override
    public Collection<Deposit> getDeposits() throws DataQueryException {
        try {
            return callGetMany(Deposit.class,
                               Queries.isNotNull(Deposit.class.getDeclaredField("depositId")),
                               Deposit::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Deposit.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Instrument getInstrumentById(String instrumentId) throws DataQueryException {
        try {
            return callGetSingle(Instrument.class,
                                 Queries.equals(Instrument.class.getDeclaredField("instrumentId"), instrumentId),
                                 Instrument::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Instrument.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Instrument> getInstrumentsByExchangeId(String exchangeId) throws DataQueryException {
        try {
            return callGetMany(Instrument.class,
                               Queries.equals(Instrument.class.getDeclaredField("exchangeId"), exchangeId),
                               Instrument::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Instrument.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Margin getMarginById(Long marginId) throws DataQueryException {
        try {
            return callGetSingle(Margin.class,
                                 Queries.equals(Margin.class.getDeclaredField("marginId"), marginId),
                                 Margin::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Margin.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Margin> getMargins() throws DataQueryException {
        try {
            return callGetMany(Margin.class,
                               Queries.isNotNull(Margin.class.getDeclaredField("marginId")),
                               Margin::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Margin.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Margin> getMarginsByOrderId(long orderId) throws DataQueryException {
        try {
            return callGetMany(Margin.class,
                               Queries.equals(Margin.class.getDeclaredField("orderId"), orderId),
                               Margin::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Margin.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Margin> getMarginsByStatus(Integer status) throws DataQueryException {
        try {
            return callGetMany(Margin.class,
                               Queries.equals(Margin.class.getDeclaredField("status"), status),
                               Margin::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Margin.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Request getRequestByOrderId(long orderId) throws DataQueryException {
        try {
            return callGetSingle(Request.class,
                                 Queries.equals(Request.class.getDeclaredField("orderId"), orderId),
                                 Request::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Request.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Request> getRequests() throws DataQueryException {
        try {
            return callGetMany(Request.class,
                               Queries.isNotNull(Request.class.getDeclaredField("orderId")),
                               Request::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Request.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Response getResponseById(long responseId) throws DataQueryException {
        try {
            return callGetSingle(Response.class,
                                 Queries.equals(Response.class.getDeclaredField("responseId"), responseId),
                                 Response::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Response.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Response> getResponseByOrderId(long orderId) throws DataQueryException {
        try {
            return callGetMany(Response.class,
                               Queries.equals(Response.class.getDeclaredField("orderId"), orderId),
                               Response::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Response.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Response> getResponses() throws DataQueryException {
        try {
            return callGetMany(Response.class,
                               Queries.isNotNull(Response.class.getDeclaredField("responseId")),
                               Response::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Response.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public SettlementPrice getSettlementPriceByInstrumentId(String instrumentId) throws DataQueryException {
        try {
            return callGetSingle(SettlementPrice.class,
                                 Queries.equals(SettlementPrice.class.getDeclaredField("instrumentId"), instrumentId),
                                 SettlementPrice::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(SettlementPrice.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Connection getSqlConnection() {
        return conn();
    }

    @Override
    public Trade getTradeById(Long tradeId) throws DataQueryException {
        try {
            return callGetSingle(Trade.class,
                                 Queries.equals(Trade.class.getDeclaredField("tradeId"), tradeId),
                                 Trade::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Trade.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Trade> getTrades() throws DataQueryException {
        try {
            return callGetMany(Trade.class,
                               Queries.isNotNull(Trade.class.getDeclaredField("tradeId")),
                               Trade::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Trade.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Trade> getTradesByOrderId(long orderId) throws DataQueryException {
        try {
            return callGetMany(Trade.class,
                               Queries.equals(Trade.class.getDeclaredField("orderId"), orderId),
                               Trade::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Trade.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public TradingDay getTradingDay() throws DataQueryException {
        try {
            return callGetSingle(TradingDay.class,
                                 Queries.isNotNull(TradingDay.class.getDeclaredField("tradingDayId")),
                                 TradingDay::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(TradingDay.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public Collection<Withdraw> getWithdraws() throws DataQueryException {
        try {
            return callGetMany(Withdraw.class,
                               Queries.isNotNull(Withdraw.class.getDeclaredField("withdrawId")),
                               Withdraw::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataQueryException(Withdraw.class.getCanonicalName(),
                                         ex);
        } catch (DbaException | SQLException | DataException ex) {
            throw new DataQueryException(ex.getMessage(),
                                         ex);
        }
    }

    @Override
    public void removeCommission(long commissionId) throws DataRemovalException {
        try {
            callRemove(Commission.class,
                       "commissionId",
                       commissionId,
                       Commission::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataRemovalException(Commission.class.getCanonicalName(),
                                           ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataRemovalException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public void removeContract(long contractId) throws DataRemovalException {
        try {
            callRemove(Contract.class,
                       "contractId",
                       contractId,
                       Contract::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataRemovalException(Contract.class.getCanonicalName(),
                                           ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataRemovalException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public void removeDeposit(long depositId) throws DataRemovalException {
        try {
            callRemove(Deposit.class,
                       "depositId",
                       depositId,
                       Deposit::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataRemovalException(Deposit.class.getCanonicalName(),
                                           ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataRemovalException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public void removeInstrument(String instrumentId) throws DataRemovalException {
        try {
            callRemove(Instrument.class,
                       "instrumentId",
                       instrumentId,
                       Instrument::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataRemovalException(Instrument.class.getCanonicalName(),
                                           ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataRemovalException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public void removeMargin(long marginId) throws DataRemovalException {
        try {
            callRemove(Margin.class,
                       "marginId",
                       marginId,
                       Margin::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataRemovalException(Margin.class.getCanonicalName(),
                                           ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataRemovalException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public void removeSettlementPrice(String instrumentId) throws DataRemovalException {
        try {
            callRemove(SettlementPrice.class,
                       "instrumentId",
                       instrumentId,
                       SettlementPrice::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataRemovalException(SettlementPrice.class.getCanonicalName(),
                                           ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataRemovalException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public void removeWithdraw(long withdrawId) throws DataRemovalException {
        try {
            callRemove(Withdraw.class,
                       "withdrawId",
                       withdrawId,
                       Withdraw::new);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataRemovalException(Withdraw.class.getCanonicalName(),
                                           ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataRemovalException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public void updateAccount(Account account) throws DataUpdateException {
        try {
            callUpdate(Account.class,
                       account,
                       Account.class.getDeclaredField("accountId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(Account.class.getCanonicalName(),
                                          ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public void updateCommission(Commission commission) throws DataUpdateException {
        try {
            callUpdate(Commission.class,
                       commission,
                       Commission.class.getDeclaredField("commissionId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(Commission.class.getCanonicalName(),
                                          ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public void updateContract(Contract contract) throws DataUpdateException {
        try {
            callUpdate(Contract.class,
                       contract,
                       Contract.class.getDeclaredField("contractId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(Contract.class.getCanonicalName(),
                                          ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public void updateInstrument(Instrument instrument) throws DataUpdateException {
        try {
            callUpdate(Instrument.class,
                       instrument,
                       Instrument.class.getDeclaredField("instrumentId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(Instrument.class.getCanonicalName(),
                                          ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public void updateMargin(Margin margin) throws DataUpdateException {
        try {
            callUpdate(Margin.class,
                       margin,
                       Margin.class.getDeclaredField("marginId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(Margin.class.getCanonicalName(),
                                          ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public void updateSettlementPrice(SettlementPrice price) throws DataUpdateException {
        try {
            callUpdate(SettlementPrice.class,
                       price,
                       SettlementPrice.class.getDeclaredField("instrumentId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(SettlementPrice.class.getCanonicalName(),
                                          ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public void updateTradingDay(TradingDay day) throws DataUpdateException {
        try {
            callUpdate(TradingDay.class,
                       day,
                       TradingDay.class.getDeclaredField("tradingDayId"));
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DataUpdateException(TradingDay.class.getCanonicalName(),
                                          ex);
        } catch (EventException | SQLException | DataException ex) {
            throw new DataUpdateException(ex.getMessage(),
                                          ex);
        }

    }

    private <T> Collection<T> callGetMany(Class<T> clazz,
                                          ICondition<?> condition,
                                          IDefaultFactory<T> factory) throws SQLException,
                                                                             DataQueryException {
        try {
            return query.select(clazz, condition, factory);
        } catch (DbaException ex) {
            throw new DataQueryException(clazz.getCanonicalName(),
                                         ex);
        }
    }

    private <T> T callGetSingle(Class<T> clazz,
                                ICondition<?> condition,
                                IDefaultFactory<T> factory) throws SQLException,
                                                                   InvalidQueryResultException,
                                                                   DataQueryException {
        Collection<T> c = callGetMany(clazz,
                                      condition,
                                      factory);
        if (c.size() > 1) {
            throw new InvalidQueryResultException("Result not single.");
        }
        if (c.isEmpty()) {
            throw new InvalidQueryResultException("Empty result.");
        }
        return c.iterator().next();
    }

    private <T> void callInsert(Class<T> clazz,
                                T object) throws DataQueryException,
                                                 SQLException,
                                                 EventException,
                                                 UnknownDataChangeException {
        try {
            query.insert(clazz, object);
            callOnChange(clazz,
                         object,
                         DataChangeType.CREATE);
        } catch (DbaException ex) {
            throw new DataQueryException(clazz.getCanonicalName(),
                                         ex);
        }
    }

    private <T> void callOnChange(Class<T> clazz,
                                  T object,
                                  DataChangeType type) throws EventException,
                                                              UnknownDataChangeException {
        getDataSource().getEventSource(type).publish(clazz,
                                                     object);
    }

    private <T, V> void callRemove(Class<T> clazz,
                                   String fieldName,
                                   V id,
                                   IDefaultFactory<T> factory) throws SQLException,
                                                                      EventException,
                                                                      NoSuchFieldException,
                                                                      DataQueryException,
                                                                      UnknownDataChangeException,
                                                                      InvalidQueryResultException {
        try {
            callOnChange(clazz,
                         callGetSingle(clazz,
                                       Queries.equals(clazz.getDeclaredField(fieldName), id),
                                       factory),
                         DataChangeType.DELETE);
            query.remove(clazz,
                         Queries.equals(clazz.getDeclaredField(fieldName), id));
        } catch (DbaException ex) {
            throw new DataQueryException(clazz.getCanonicalName(),
                                         ex);
        }
    }

    private <T> void callUpdate(Class<T> clazz,
                                T object,
                                Field field) throws DataQueryException,
                                                    SQLException,
                                                    EventException,
                                                    UnknownDataChangeException {
        try {
            query.update(clazz,
                         object,
                         Queries.equals(field, field.getLong(object)));
            callOnChange(clazz,
                         object,
                         DataChangeType.UPDATE);
        } catch (DbaException ex) {
            throw new DataQueryException(clazz.getCanonicalName(),
                                         ex);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new DataQueryException(ex);
        }
    }
}
