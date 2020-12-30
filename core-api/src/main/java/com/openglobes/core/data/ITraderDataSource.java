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

import com.openglobes.core.event.IEventHandler;
import com.openglobes.core.event.IEventSource;
import java.util.Properties;

/**
 * Data source provides basic data to higher level.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface ITraderDataSource {
    void setProperties(Properties properties) throws DataSourceException;

    ITraderData getConnection() throws DataSourceException;

    <T> void addListener(Class<T> clazz, IEventHandler<T> handler, DataChangeType type) throws DataSourceException;

    <T> IEventSource getEventSource(DataChangeType type) throws DataSourceException;
}
