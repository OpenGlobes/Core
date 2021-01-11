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
package com.openglobes.core.dba;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IDataSource {

    /**
     * Find next available SQL connection.
     *
     * @return SQL connection.
     *
     * @throws ClassNotFoundException thrown by {@link Class#forName}.
     * @throws SQLException           thrown by
     *                                {@link java.sql.DriverManager#getConnection(java.lang.String, java.util.Properties)}.
     */
    Connection findConnection() throws ClassNotFoundException, SQLException;

    /**
     * Free the binding of the specified connection to the
     * {@link IDataConnection} object and return available for new connection.
     *
     * @param connection SQl connection.
     *
     * @throws RuntimeException thrown when the specified connection is not
     *                          created by the datasource.
     */
    void freeConnection(Connection connection) throws RuntimeException;

    /**
     * Get properites used on obtaining connection by
     * {@link java.sql.DriverManager#getConnection(java.lang.String, java.util.Properties)}.
     *
     * @return properties used on obtaining connection.
     */
    Properties getConnectionProperties();

    /**
     * Get original properities in configuration on the data source.
     *
     * @return original properties for the data source.
     */
    Properties getProperties();

}
