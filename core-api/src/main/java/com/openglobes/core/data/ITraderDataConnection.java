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

    void addAccount(Account account) throws DataInsertionException;

    void addCommission(Commission commission) throws DataInsertionException;

    void addContract(Contract contract) throws DataInsertionException;

    void addDeposit(Deposit deposit) throws DataInsertionException;

    void addInstrument(Instrument instrument) throws DataInsertionException;

    void addMargin(Margin margin) throws DataInsertionException;

    void addRequest(Request request) throws DataInsertionException;

    void addResponse(Response response) throws DataInsertionException;

    void addSettlementPrice(SettlementPrice price) throws DataInsertionException;

    void addTrade(Trade trade) throws DataInsertionException;

    void addTradingDay(TradingDay day) throws DataInsertionException;

    void addWithdraw(Withdraw withdraw) throws DataInsertionException;

    @Override
    void close();

    Account getAccount() throws DataQueryException;

    Commission getCommissionById(Long commissionId) throws DataQueryException;

    Collection<Commission> getCommissions() throws DataQueryException;

    Collection<Commission> getCommissionsByOrderId(long orderId) throws DataQueryException;

    Collection<Commission> getCommissionsByStatus(Integer status) throws DataQueryException;

    Contract getContractById(Long contractId) throws DataQueryException;

    Collection<Contract> getContracts() throws DataQueryException;

    Collection<Contract> getContractsByInstrumentId(String instrumentId) throws DataQueryException;

    Collection<Contract> getContractsByStatus(Integer status) throws DataQueryException;

    Collection<Contract> getContractsByTradeId(long tradeId) throws DataQueryException;

    ITraderDataSource getDataSource();

    Collection<Deposit> getDeposits() throws DataQueryException;

    Instrument getInstrumentById(String instrumentId) throws DataQueryException;

    Collection<Instrument> getInstrumentsByExchangeId(String exchangeId) throws DataQueryException;

    Margin getMarginById(Long marginId) throws DataQueryException;

    Collection<Margin> getMargins() throws DataQueryException;

    Collection<Margin> getMarginsByOrderId(long orderId) throws DataQueryException;

    Collection<Margin> getMarginsByStatus(Integer status) throws DataQueryException;

    Request getRequestByOrderId(long orderId) throws DataQueryException;

    Collection<Request> getRequests() throws DataQueryException;

    Response getResponseById(long responseId) throws DataQueryException;

    Collection<Response> getResponseByOrderId(long orderId) throws DataQueryException;

    Collection<Response> getResponses() throws DataQueryException;

    SettlementPrice getSettlementPriceByInstrumentId(String instrumentId) throws DataQueryException;

    Connection getSqlConnection();

    Trade getTradeById(Long tradeId) throws DataQueryException;

    Collection<Trade> getTrades() throws DataQueryException;

    Collection<Trade> getTradesByOrderId(long orderId) throws DataQueryException;

    TradingDay getTradingDay() throws DataQueryException;

    Collection<Withdraw> getWithdraws() throws DataQueryException;

    void removeCommission(long commissionId) throws DataRemovalException;

    void removeContract(long contractId) throws DataRemovalException;

    void removeDeposit(long depositId) throws DataRemovalException;

    void removeInstrument(String instrumentId) throws DataRemovalException;

    void removeMargin(long marginId) throws DataRemovalException;

    void removeSettlementPrice(String instrumentId) throws DataRemovalException;

    void removeWithdraw(long withdrawId) throws DataRemovalException;

    void updateAccount(Account account) throws DataUpdateException;

    void updateCommission(Commission commission) throws DataUpdateException;

    void updateContract(Contract contract) throws DataUpdateException;

    void updateInstrument(Instrument instrument) throws DataUpdateException;

    void updateMargin(Margin margin) throws DataUpdateException;

    void updateSettlementPrice(SettlementPrice price) throws DataUpdateException;

    void updateTradingDay(TradingDay day) throws DataUpdateException;
}
