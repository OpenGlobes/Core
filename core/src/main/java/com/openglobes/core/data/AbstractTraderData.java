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

import com.openglobes.core.exceptions.Exceptions;
import java.lang.ref.Cleaner;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public abstract class AbstractTraderData implements ITraderData {

    private static final Cleaner cleaner = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private final Connection conn;
    private final AbstractTraderDataSource src;

    public AbstractTraderData(Connection connection, AbstractTraderDataSource source) throws DataSourceException {
        if (connection == null) {
            throw new DataSourceException(Exceptions.DATA_CONNECTION_NULL.code(),
                                          Exceptions.DATA_CONNECTION_NULL.message());
        }
        if (source == null) {
            throw new DataSourceException(Exceptions.DATASOURCE_NULL.code(),
                                          Exceptions.DATASOURCE_NULL.message());
        }
        conn = connection;
        src = source;
        cleanable = cleaner.register(this, new CleanAction(conn, src));
    }

    @Override
    public void close() throws Exception {
        cleanable.clean();
    }

    protected Connection conn() {
        return this.conn;
    }

    protected ITraderDataSource source() {
        return this.src;
    }

    private static class CleanAction implements Runnable {

        private final Connection conn;
        private final AbstractTraderDataSource src;

        CleanAction(Connection connection, AbstractTraderDataSource source) {
            conn = connection;
            src = source;
        }

        @Override
        public void run() {
            try {
                src.freeConnection(conn);
            }
            catch (DataSourceException ex) {
                Logger.getLogger(AbstractTraderDataSource.class.getName()).log(Level.SEVERE, 
                                                                               ex.toString(),
                                                                               ex);
            }
        }

    }
}
