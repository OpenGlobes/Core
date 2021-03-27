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

import com.openglobes.core.event.EventSource;
import com.openglobes.core.event.IEventHandler;
import com.openglobes.core.event.IEventSource;
import com.openglobes.core.event.InvalidSubscriptionException;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class DefaultTraderDataSource extends AbstractTraderDataSource {

    private final Map<DataChangeType, IEventSource> events;

    public DefaultTraderDataSource() {
        events = new ConcurrentHashMap<>(DataChangeType.values().length);
        setupEvents();
    }

    @Override
    public <T> void addListener(Class<T> clazz,
                                IEventHandler<T> handler,
                                DataChangeType type)
            throws UnknownDataChangeException,
                   InvalidSubscriptionException {
        getEventSource(type).subscribe(clazz, handler);
    }

    @Override
    public AbstractTraderDataConnection getConnection() throws SQLException,
                                                               ClassNotFoundException {
        return new DefaultTraderDataConnection(getSqlConnection(), this);
    }

    @Override
    public IEventSource getEventSource(DataChangeType type) throws UnknownDataChangeException {
        if (!events.containsKey(type)) {
            throw new UnknownDataChangeException(type.name());
        }
        return events.get(type);
    }

    private void setupEvents() {
        for (var c : DataChangeType.values()) {
            events.put(c, new EventSource());
        }
    }
}
