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

import com.openglobes.core.dba.AbstractDataSource;
import com.openglobes.core.event.EventSource;
import com.openglobes.core.event.EventSourceException;
import com.openglobes.core.event.IEventHandler;
import com.openglobes.core.event.IEventSource;
import com.openglobes.core.exceptions.Exceptions;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class TraderDataSource extends AbstractDataSource implements ITraderDataSource{

    private final Map<DataChangeType, EventSource> events;

    public TraderDataSource() {
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
            throw new DataSourceException(Exceptions.SUBSCRIBE_EVENT_FAIL.code(),
                                          ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public ITraderData getConnection() throws DataSourceException {
        try {
            return new TraderData(findConnection(), this);
        }
        catch (ClassNotFoundException ex) {
            throw new DataSourceException(Exceptions.DATASOURCE_DRIVER_CLASS_MISSING.code(),
                                          Exceptions.DATASOURCE_DRIVER_CLASS_MISSING.message(),
                                          ex);
        }
        catch (SQLException ex) {
            throw new DataSourceException(ex.getErrorCode(),
                                          ex.getMessage(),
                                          ex);
        }
    }

    @Override
    public IEventSource getEventSource(DataChangeType type) throws DataSourceException {
        if (!events.containsKey(type)) {
            throw new DataSourceException(Exceptions.DATASOURCE_EVENTSOURCE_NOT_FOUND.code(),
                                          Exceptions.DATASOURCE_EVENTSOURCE_NOT_FOUND.message());
        }
        return events.get(type);
    }

    @Override
    public void open(Properties properties) throws DataSourceException {
        props().clear();
        props().putAll(properties);
    }

    private void setupEvents() {
        for (var c : DataChangeType.values()) {
            events.put(c, new EventSource());
        }
    }
}
