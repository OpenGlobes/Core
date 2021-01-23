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
package com.openglobes.core.dba;

import com.openglobes.core.utils.Loggers;

import java.lang.ref.Cleaner;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public abstract class AbstractPooledConnection implements AutoCloseable,
                                                          IPooledConnection {

    private static final Cleaner           cleaner = Cleaner.create();
    private final        Cleaner.Cleanable cleanable;
    private final        Connection        conn;
    private final        IPooledDataSource src;
    private              Boolean           exAutoCommit;

    public AbstractPooledConnection(Connection connection,
                                    IPooledDataSource source) {
        Objects.requireNonNull(connection);
        Objects.requireNonNull(source);
        conn      = connection;
        src       = source;
        cleanable = cleaner.register(this, new CleanAction(conn, src));
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    @Override
    public void commit() throws SQLException {
        try {
            conn().commit();
        } finally {
            restoreTransaction();
        }
    }

    @Override
    public IPooledDataSource getSource() {
        return this.src;
    }

    @Override
    public void rollback() throws SQLException {
        try {
            conn().rollback();
        } finally {
            restoreTransaction();
        }
    }

    @Override
    public void transaction() throws SQLException {
        try {
            exAutoCommit = conn().getAutoCommit();
            conn().setAutoCommit(false);
        } catch (SQLException ex) {
            restoreTransaction();
            throw ex;
        }
    }

    private void restoreTransaction() throws SQLException {
        if (exAutoCommit != null) {
            conn().setAutoCommit(exAutoCommit);
        }
    }

    protected Connection conn() {
        return this.conn;
    }

    private static class CleanAction implements Runnable {

        private final Connection        conn;
        private final IPooledDataSource src;

        CleanAction(Connection connection, IPooledDataSource source) {
            conn = connection;
            src  = source;
        }

        @Override
        public void run() {
            try {
                src.ungetSqlConnection(conn);
            } catch (Throwable th) {
                Loggers.getLogger(AbstractPooledDataSource.class.getCanonicalName()).log(Level.SEVERE,
                                                                                         th.getMessage(),
                                                                                         th);
            }
        }

    }
}
