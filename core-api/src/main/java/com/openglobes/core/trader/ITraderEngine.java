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

import com.openglobes.core.GatewayException;
import com.openglobes.core.ServiceRuntimeStatus;
import com.openglobes.core.data.ITraderDataSource;
import com.openglobes.core.event.IEventSource;

import java.util.Collection;
import java.util.Properties;

/**
 * Trader engine interface.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface ITraderEngine {

    void enableTrader(int traderId, boolean enabled) throws UnknownTraderIdException;

    ITraderEngineAlgorithm getAlgorithm();

    void setAlgorithm(ITraderEngineAlgorithm algo);

    ITraderDataSource getDataSource();

    void setDataSource(ITraderDataSource dataSource);

    IEventSource getEventSource();

    Instrument getTodayInstrument(String instrumentId);

    Collection<Instrument> getTodayInstruments();

    ServiceRuntimeStatus getStatus();

    TraderGatewayContext getTraderGatewayContext(int traderId) throws UnknownTraderIdException;

    Collection<TraderGatewayContext> getTraderGatewayContexts();

    void settle() throws SettlementException;

    void renew() throws TraderRenewException;

    void registerTrader(int traderId,
                        ITraderGateway trader) throws DuplicatedTraderIdException,
                                                      GatewayException;

    void request(Request request,
                 Instrument instrument,
                 Properties properties) throws IllegalRequestException,
                                       InvalidRequestException;

    void unregisterTrader(int traderId) throws UnknownTraderIdException;

}
