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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public abstract class AbstractDataSource implements AutoCloseable, IDataSource {

    private static final Cleaner cleaner = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private final Map<Connection, Boolean> free = new HashMap<>(128);
    private final Properties props;

    protected AbstractDataSource() {
        props = new Properties();
        cleanable = cleaner.register(this, new CleanAction(free));
    }

    @Override
    public void close() throws Exception {
        cleanable.clean();
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

    protected Properties props() {
        return props;
    }

    @Override
    public Connection findConnection() throws ClassNotFoundException,
                                       SQLException {
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
    public void freeConnection(Connection connection) throws RuntimeException {
        synchronized (free) {
            if (!free.containsKey(connection)) {
                throw new RuntimeException("Unkown connection object.");
            }
            free.put(connection, Boolean.TRUE);
        }
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
                    }
                    catch (SQLException ex) {
                        Logger.getLogger(AbstractDataSource.class.getName()).log(Level.SEVERE,
                                                                                       ex.toString(),
                                                                                       ex);
                    }
                });
                m.clear();
            }
        }

    }
}
