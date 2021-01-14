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

import java.lang.ref.Cleaner;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public abstract class AbstractPooledConnection implements AutoCloseable,
                                                          IPooledConnection {

    private static final Cleaner cleaner = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private final Connection conn;
    private Boolean exAutoCommit;
    private final IPooledDataSource src;

    public AbstractPooledConnection(Connection connection,
                                    IPooledDataSource source) {
        Objects.requireNonNull(connection);
        Objects.requireNonNull(source);
        conn = connection;
        src = source;
        cleanable = cleaner.register(this, new CleanAction(conn, src));
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    @Override
    public void commit() throws DbaException {
        try {
            conn().commit();
        }
        catch (SQLException ex) {
            throw new DbaException(ex.getMessage(),
                                   ex);
        }
        finally {
            restoreTransaction();
        }
    }

    @Override
    public IPooledDataSource getSource() {
        return this.src;
    }

    @Override
    public void rollback() throws DbaException {
        try {
            conn().rollback();
        }
        catch (SQLException ex) {
            throw new DbaException(ex.getMessage(),
                                   ex);
        }
        finally {
            restoreTransaction();
        }
    }

    @Override
    public void transaction() throws DbaException {
        try {
            exAutoCommit = conn().getAutoCommit();
            conn().setAutoCommit(false);
        }
        catch (SQLException ex) {
            restoreTransaction();
            throw new DbaException(ex.getMessage(),
                                   ex);
        }
    }

    private void restoreTransaction() throws DbaException {
        try {
            if (exAutoCommit != null) {
                conn().setAutoCommit(exAutoCommit);
            }
        }
        catch (SQLException ex) {
            throw new DbaException(ex.getMessage(),
                                   ex);
        }
    }

    protected Connection conn() {
        return this.conn;
    }

    private static class CleanAction implements Runnable {

        private final Connection conn;
        private final IPooledDataSource src;

        CleanAction(Connection connection, IPooledDataSource source) {
            conn = connection;
            src = source;
        }

        @Override
        public void run() {
            try {
                src.ungetSqlConnection(conn);
            }
            catch (Throwable th) {
                Logger.getLogger(AbstractPooledDataSource.class.getName()).log(Level.SEVERE,
                                                                               th.getMessage(),
                                                                               th);
            }
        }

    }
}
