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

import com.openglobes.core.dba.IPooledConnection;
import com.openglobes.core.trader.Account;
import com.openglobes.core.trader.Commission;
import com.openglobes.core.trader.Contract;
import com.openglobes.core.trader.Deposit;
import com.openglobes.core.trader.Instrument;
import com.openglobes.core.trader.Margin;
import com.openglobes.core.trader.Request;
import com.openglobes.core.trader.Response;
import com.openglobes.core.trader.SettlementPrice;
import com.openglobes.core.trader.Trade;
import com.openglobes.core.trader.TradingDay;
import com.openglobes.core.trader.Withdraw;
import java.sql.Connection;
import java.util.Collection;

/**
 * Data connection.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface ITraderDataConnection extends AutoCloseable,
                                               IPooledConnection {

    void addAccount(Account account) throws DataSourceException;

    void addCommission(Commission commission) throws DataSourceException;

    void addContract(Contract contract) throws DataSourceException;

    void addDeposit(Deposit deposit) throws DataSourceException;

    void addInstrument(Instrument instrument) throws DataSourceException;

    void addMargin(Margin margin) throws DataSourceException;

    void addRequest(Request request) throws DataSourceException;

    void addResponse(Response response) throws DataSourceException;

    void addSettlementPrice(SettlementPrice price) throws DataSourceException;

    void addTrade(Trade trade) throws DataSourceException;

    void addTradingDay(TradingDay day) throws DataSourceException;

    void addWithdraw(Withdraw withdraw) throws DataSourceException;

    @Override
    void close();

    Account getAccount() throws DataSourceException;

    Commission getCommissionById(Long commissionId) throws DataSourceException;

    Collection<Commission> getCommissions() throws DataSourceException;

    Collection<Commission> getCommissionsByOrderId(long orderId) throws DataSourceException;

    Collection<Commission> getCommissionsByStatus(Integer status) throws DataSourceException;

    Contract getContractById(Long contractId) throws DataSourceException;

    Collection<Contract> getContracts() throws DataSourceException;

    Collection<Contract> getContractsByInstrumentId(String instrumentId) throws DataSourceException;

    Collection<Contract> getContractsByStatus(Integer status) throws DataSourceException;

    Collection<Contract> getContractsByTradeId(long tradeId) throws DataSourceException;

    ITraderDataSource getDataSource() throws DataSourceException;

    Collection<Deposit> getDeposits() throws DataSourceException;

    Instrument getInstrumentById(String instrumentId) throws DataSourceException;

    Collection<Instrument> getInstrumentsByExchangeId(String exchangeId) throws DataSourceException;

    Margin getMarginById(Long marginId) throws DataSourceException;

    Collection<Margin> getMargins() throws DataSourceException;

    Collection<Margin> getMarginsByOrderId(long orderId) throws DataSourceException;

    Collection<Margin> getMarginsByStatus(Integer status) throws DataSourceException;

    Request getRequestByOrderId(long orderId) throws DataSourceException;

    Collection<Request> getRequests() throws DataSourceException;

    Response getResponseById(long responseId) throws DataSourceException;

    Collection<Response> getResponseByOrderId(long orderId) throws DataSourceException;

    Collection<Response> getResponses() throws DataSourceException;

    SettlementPrice getSettlementPriceByInstrumentId(String instrumentId) throws DataSourceException;

    Connection getSqlConnection() throws DataSourceException;

    Trade getTradeById(Long tradeId) throws DataSourceException;

    Collection<Trade> getTrades() throws DataSourceException;

    Collection<Trade> getTradesByOrderId(long orderId) throws DataSourceException;

    TradingDay getTradingDay() throws DataSourceException;

    Collection<Withdraw> getWithdraws() throws DataSourceException;

    void removeCommission(long commissionId) throws DataSourceException;

    void removeContract(long contractId) throws DataSourceException;

    void removeDeposit(long depositId) throws DataSourceException;

    void removeInstrument(String instrumentId) throws DataSourceException;

    void removeMargin(long marginId) throws DataSourceException;

    void removeSettlementPrice(String instrumentId) throws DataSourceException;

    void removeWithdraw(long withdrawId) throws DataSourceException;

    void updateAccount(Account account) throws DataSourceException;

    void updateCommission(Commission commission) throws DataSourceException;

    void updateContract(Contract contract) throws DataSourceException;

    void updateInstrument(Instrument instrument) throws DataSourceException;

    void updateMargin(Margin margin) throws DataSourceException;

    void updateSettlementPrice(SettlementPrice price) throws DataSourceException;

    void updateTradingDay(TradingDay day) throws DataSourceException;
}
