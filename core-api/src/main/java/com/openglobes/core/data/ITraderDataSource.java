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

import com.openglobes.core.dba.IPooledDataSource;
import com.openglobes.core.event.IEventHandler;
import com.openglobes.core.event.IEventSource;
import com.openglobes.core.event.InvalidSubscriptionException;

import java.sql.SQLException;

/**
 * Data source provides basic data to higher level.
 * <p>
 * The data source is open for all JDBC compatible drivers. It loads driver by
 * calling {@link Class#forName(java.lang.String)} where parameter is specified
 * in properties with the key {@code DataSource.DriverClass}.
 * <p>
 * The URL in call
 * {@link java.sql.DriverManager#getConnection(java.lang.String, java.util.Properties)}
 * is specified in properties by the key {@code DataSource.URL} and the rest of
 * the properties are passed directly as second parameter.
 * <p>
 * To use a JDBC driver, set class path to contain that driver when building the
 * application, or load that driver programmatically.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface ITraderDataSource extends AutoCloseable,
                                           IPooledDataSource {

    /**
     * Get {@link ITraderDataConnection} of the underlying data source with the
     * given properties.
     * <p>
     * The return Object could be retieved from internal pool or newed object if
     * the pool is all used.
     *
     * @return {@link ITraderDataConnection}
     * @throws java.sql.SQLException thrown on failing getting connection from
     * driver manager.
     * @throws java.lang.ClassNotFoundException thrown on failing loading driver
     * class.
     */
    ITraderDataConnection getConnection() throws SQLException,
                                                 ClassNotFoundException;

    /**
     * Don't throw exception.
     */
    @Override
    void close();

    /**
     * Add {@link IEventHandler<T>} for the specified class on the specified
     * {@link DataChangeType} type.
     *
     * @param <T>     Type of event to listen on.
     * @param clazz   Class of the specified type.
     * @param handler Event handler.
     * @param type    Data change type.
     * @throws UnknownDataChangeException thrown when the specified data change
     * type has no associated event source.
     * @throws InvalidSubscriptionException thrown on failing subscribing to
     * event source.
     */
    <T> void addListener(Class<T> clazz,
                         IEventHandler<T> handler,
                         DataChangeType type) throws UnknownDataChangeException,
                                                     InvalidSubscriptionException;

    /**
     * Get the event source associated with the specified data change type.
     *
     * @param type Data change type.
     * @return {@link  IEventSource} associated with the specified data change
     * type.
     * @throws UnknownDataChangeException thrown when no event source for the
     * specifed data change type.
     */
    IEventSource getEventSource(DataChangeType type) throws UnknownDataChangeException;
}
