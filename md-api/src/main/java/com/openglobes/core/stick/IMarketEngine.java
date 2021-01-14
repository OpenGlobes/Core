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
package com.openglobes.core.stick;

import com.openglobes.core.data.IMarketDataSource;
import com.openglobes.core.event.IEventSource;
import com.openglobes.core.exceptions.EngineException;
import java.util.Properties;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IMarketEngine {

    IEventSource getEventSource() throws EngineException;

    void setDataSource(IMarketDataSource dataSource) throws EngineException;

    void registerMarket(int marketId, IMarketGateway gateway) throws EngineException;

    void unregisterMarket(int marketId) throws EngineException;

    void start(Properties properties) throws EngineException;

    void stop() throws EngineException;
}
