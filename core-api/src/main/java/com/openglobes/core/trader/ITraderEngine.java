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

import com.openglobes.core.data.ITraderDataSource;
import com.openglobes.core.event.IEventSource;
import com.openglobes.core.exceptions.EngineException;
import com.openglobes.core.exceptions.ServiceRuntimeStatus;
import java.util.Collection;
import java.util.Properties;

/**
 * Trader engine interface.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface ITraderEngine {

    void enableTrader(int traderId, boolean enabled) throws EngineException;

    ITraderEngineAlgorithm getAlgorithm();

    void setAlgorithm(ITraderEngineAlgorithm algo) throws EngineException;

    ITraderDataSource getDataSource();

    void setDataSource(ITraderDataSource dataSource) throws EngineException;

    IEventSource getEventSource() throws EngineException;

    void setEventSource(IEventSource eventSource) throws EngineException;

    Instrument getRelatedInstrument(String instrumentId) throws EngineException;

    ServiceRuntimeStatus getStatus();

    TraderGatewayRuntime getTraderServiceInfo(int traderId) throws EngineException;

    Collection<TraderGatewayRuntime> getTraderServiceRuntimes() throws EngineException;

    void settle(Properties properties) throws EngineException;

    void initialize(Properties properties) throws EngineException;

    void registerTrader(int traderId, ITraderGateway trader) throws EngineException;

    void request(Request request, Instrument instrument, Properties properties, int requestId) throws EngineException;

    void request(Request request, int requestId) throws EngineException;

    void setInitProperties(int traderId, Properties properties) throws EngineException;

    void setSettleProperties(int traderId, Properties properties) throws EngineException;

    void setStartProperties(int traderId, Properties properties) throws EngineException;

    void start(Properties properties) throws EngineException;

    void stop() throws EngineException;

    void unregisterTrader(int traderId) throws EngineException;

}
