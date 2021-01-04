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
 * <p>
 * The data source is open for all JDBC compatible drivers. It loads driver by
 * calling {@link Class#forName(java.lang.String)} where parameter is specified
 * in properties with the key {@code DataSource.DriverClass}.
 * <p>
 * The URL in call
 * {@link java.sql.DriverManager#getConnection(java.lang.String, java.util.Properties)}
 * is specified in properties by the key {@code DataSource.URL} and the rest
 * of the properties are passed directly as second parameter.
 * <p>
 * To usea JDBC driver, set class path to contain that driver when building the
 * application, or load that driver programatically.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface ITraderDataSource extends AutoCloseable {

    /**
     * Set properties for initialiazing data source and creating connection.
     * <p>
     * Properties are in align with
     * {@link java.sql.DriverManager#getConnection(java.lang.String, java.util.Properties)}
     * except the following dedicated properties:
     * <ul>
     * <li><b>DataSource.URL</b>:URL of the connection.
     * <li><b>DataSource.DriverClass</b>:Driver class canonical name used in {@link Class#forName(java.lang.String)
     * }
     * </ul>
     * The dedicated properties are removed before being used for connection.
     * and the rest of properties are directly passed to method.
     *
     * @param properties properties for JDBC connection including dedicated
     *                   properies for URL and driver class.
     *
     * @throws DataSourceException thrown when fail loading driver class or
     *                             getting new connection.
     */
    void setProperties(Properties properties) throws DataSourceException;

    /**
     * Get {@link ITraderData} of the underlying data source with the given
     * properties.
     * <p>
     * The return Object could be retieved from internal pool or newed object if
     * the pool is all used.
     *
     * @return {@link ITraderData}
     *
     * @throws DataSourceException thrown when failing to creating
     *                             {@link ITraderData}.
     */
    ITraderData getConnection() throws DataSourceException;

    /**
     * Add {@link IEventHandler<T>} for the specified class on the specified
     * {@link DataChangeType} type.
     *
     * @param <T>     Type of event to listen on.
     * @param clazz   Class of the specified type.
     * @param handler Event handler.
     * @param type    Data change type.
     *
     * @throws DataSourceException thrown if given parameters are invalid, fail
     *                             subscribing the specified event type, or no
     *                             event source for the specified data change
     *                             type.
     */
    <T> void addListener(Class<T> clazz, IEventHandler<T> handler, DataChangeType type) throws DataSourceException;

    /**
     * Get the event source associated with the specified data change type.
     *
     * @param type Data change type.
     *
     * @return {@link  IEventSource} associated with the specified data change
     *         type.
     *
     * @throws DataSourceException thrown when no event source for the specifed
     *                             data change type.
     */
    IEventSource getEventSource(DataChangeType type) throws DataSourceException;
}
