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

import com.openglobes.core.ErrorCode;
import com.openglobes.core.dba.DbaException;
import com.openglobes.core.event.EventSource;
import com.openglobes.core.event.EventSourceException;
import com.openglobes.core.event.IEventHandler;
import com.openglobes.core.event.IEventSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class DefaultTraderDataSource extends TraderDataSource {

    private final Map<DataChangeType, IEventSource> events;

    public DefaultTraderDataSource() {
        events = new ConcurrentHashMap<>(DataChangeType.values().length);
        setupEvents();
    }

    @Override
    public <T> void addListener(Class<T> clazz,
                                IEventHandler<T> handler,
                                DataChangeType type) throws DataSourceException {
        try {
            getEventSource(type).subscribe(clazz, handler);
        }
        catch (EventSourceException ex) {
            throw new DataSourceException(ErrorCode.SUBSCRIBE_EVENT_FAIL.code(),
                                          ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public TraderDataConnection getConnection() throws DataSourceException {
        try {
            return new DefaultTraderDataConnection(getSqlConnection(), this);
        }
        catch (DbaException ex) {
            throw new DataSourceException(ErrorCode.DATASOURCE_GET_CONNECTION_FAIL.code(),
                                          ErrorCode.DATASOURCE_GET_CONNECTION_FAIL.message(),
                                          ex);
        }
    }

    @Override
    public IEventSource getEventSource(DataChangeType type) throws DataSourceException {
        if (!events.containsKey(type)) {
            throw new DataSourceException(ErrorCode.DATASOURCE_EVENTSOURCE_NOT_FOUND.code(),
                                          ErrorCode.DATASOURCE_EVENTSOURCE_NOT_FOUND.message());
        }
        return events.get(type);
    }

    private void setupEvents() {
        for (var c : DataChangeType.values()) {
            events.put(c, new EventSource());
        }
    }
}
