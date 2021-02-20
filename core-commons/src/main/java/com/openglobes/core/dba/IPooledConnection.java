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

import java.sql.SQLException;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IPooledConnection {

    /**
     * Get the data source that created this data connection and manages the
     * internal SQL connection.
     *
     * @return data source that created this data connection.
     */
    IPooledDataSource getSource();

    /**
     * Commit all queries after calling {@link transaction()}.
     *
     * @throws SQLException thrown when failing to commit the underlying
     *                      {@link java.sql.Connection}.
     */
    void commit() throws SQLException;

    /**
     * Rollback all queries after calling {@link transaction()}.
     *
     * @throws SQLException thrown when failing to rollback the underlying
     *                      {@link java.sql.Connection}.
     */
    void rollback() throws SQLException;

    /**
     * Start a transaction then all queries set after this method need to be
     * commited by calling {@link commit()}.
     *
     * @throws SQLException thrown when failing to start a transaction for the
     *                      underlying {@link java.sql.Connection}.
     */
    void transaction() throws SQLException;

}
