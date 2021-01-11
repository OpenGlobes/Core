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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public abstract class AbstractData implements AutoCloseable, IDataConnection {

    private static final Cleaner cleaner = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private final Connection conn;
    private final IDataSource src;

    public AbstractData(Connection connection, IDataSource source) {
        Objects.requireNonNull(connection);
        Objects.requireNonNull(source);
        conn = connection;
        src = source;
        cleanable = cleaner.register(this, new CleanAction(conn, src));
    }

    @Override
    public void close() throws Exception {
        cleanable.clean();
    }

    @Override
    public IDataSource getSource() {
        return this.src;
    }

    protected Connection conn() {
        return this.conn;
    }

    private static class CleanAction implements Runnable {

        private final Connection conn;
        private final IDataSource src;

        CleanAction(Connection connection, IDataSource source) {
            conn = connection;
            src = source;
        }

        @Override
        public void run() {
            try {
                src.freeConnection(conn);
            }
            catch (Throwable th) {
                Logger.getLogger(AbstractDataSource.class.getName()).log(Level.SEVERE,
                                                                         th.getMessage(),
                                                                         th);
            }
        }

    }
}
