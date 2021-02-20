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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public abstract class AbstractPooledDataSource implements AutoCloseable,
                                                          IPooledDataSource {

    private static final Cleaner cleaner = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private final Map<Connection, Boolean> free = new HashMap<>(128);
    private final Properties props;

    protected AbstractPooledDataSource() {
        props = new Properties();
        cleanable = cleaner.register(this, new CleanAction(free));
    }

    @Override
    public void close() {
        cleanable.clean();
    }

    @Override
    public Properties getConnectionProperties() {
        var r = new Properties(props);
        r.remove("DataSource.URL");
        r.remove("DataSource.DriverClass");
        return r;
    }

    @Override
    public Properties getProperties() {
        return new Properties(props);
    }

    @Override
    public Connection getSqlConnection() throws SQLException,
                                                ClassNotFoundException {
        synchronized (free) {
            for (var c : free.entrySet()) {
                if (c.getValue()) {
                    c.setValue(Boolean.FALSE);
                    return c.getKey();
                }
            }
            var c = allocateConnection();
            free.put(c, Boolean.FALSE);
            return c;
        }
    }

    @Override
    public void open(Properties properties) {
        props().clear();
        props().putAll(properties);
    }

    @Override
    public void ungetSqlConnection(Connection connection) throws UnknownConnectionException {
        synchronized (free) {
            try {
                if (!free.containsKey(connection)) {
                    throw new UnknownConnectionException("Connection not found in cache.");
                }
                connection.setAutoCommit(true);
                free.put(connection, Boolean.TRUE);
            } catch (SQLException ex) {
                /*
                 * Close the connection if we fail restoring its auto-commit
                 * state.
                 */
                try {
                    connection.close();
                } catch (SQLException se) {
                    Loggers.getLogger(AbstractPooledDataSource.class.getCanonicalName())
                            .log(Level.SEVERE,
                                 se.getMessage() + "(" + se.getErrorCode() + ")",
                                 se);
                } finally {
                    free.remove(connection);
                }
            }
        }
    }

    private Connection allocateConnection() throws ClassNotFoundException,
                                                   SQLException {
        if (free.isEmpty()) {
            Class.forName(findDriverClassName());
        }
        return DriverManager.getConnection(findURL(),
                                           getConnectionProperties());
    }

    private String findDriverClassName() {
        return props().getProperty("DataSource.DriverClass");
    }

    private String findURL() {
        return props().getProperty("DataSource.URL");
    }

    protected Properties props() {
        return props;
    }

    private static class CleanAction implements Runnable {

        private final Map<Connection, Boolean> m;

        CleanAction(Map<Connection, Boolean> map) {
            this.m = map;
        }

        @Override
        public void run() {
            synchronized (m) {
                m.keySet().forEach(c -> {
                    try {
                        c.close();
                    } catch (SQLException ex) {
                        Loggers.getLogger(AbstractPooledDataSource.class.getCanonicalName()).log(Level.SEVERE,
                                                                                                 ex.toString(),
                                                                                                 ex);
                    }
                });
                m.clear();
            }
        }

    }
}
