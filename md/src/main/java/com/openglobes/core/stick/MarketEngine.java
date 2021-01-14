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
public class MarketEngine implements IMarketEngine {

    @Override
    public void setDataSource(IMarketDataSource dataSource) throws EngineException {
        // TODO setDataSource
    }

    @Override
    public IEventSource getEventSource() throws EngineException {
        // TODO getEventSource
        return null;
    }

    @Override
    public INoticeSource getNoticeSource() throws EngineException {
        // TODO getNoticeSource
        return null;
    }

    @Override
    public void registerMarket(int marketId, IMarketGateway gateway) throws EngineException {
        // TODO registerMarket
    }

    @Override
    public void start(Properties properties) throws EngineException {
        // TODO start
    }

    @Override
    public void stop() throws EngineException {
        // TODO stop
    }

    @Override
    public void unregisterMarket(int marketId) throws EngineException {
        // TODO unregisterMarket
    }
    
}
