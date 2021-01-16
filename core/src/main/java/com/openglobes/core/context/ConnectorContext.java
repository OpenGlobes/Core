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
package com.openglobes.core.context;

import com.openglobes.core.IRequestContext;
import com.openglobes.core.session.ISessionFactory;
import com.openglobes.core.configuration.ConnectorConfiguration;
import com.openglobes.core.connector.ConnectorException;
import com.openglobes.core.connector.IConnector;
import com.openglobes.core.connector.IConnectorContext;
import com.openglobes.core.session.SessionFactory;
import java.util.Objects;
import java.util.Properties;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class ConnectorContext implements IConnectorContext {

    private final ConnectorConfiguration conf;
    private final IConnector conn;
    private final IRequestContext ctx;
    private ISessionFactory factory;

    public ConnectorContext(ConnectorConfiguration configuration,
                            IConnector connector,
                            IRequestContext context) {
        conf = configuration;
        this.conn = connector;
        this.ctx = context;
    }

    @Override
    public IConnector get() {
        return conn;
    }

    @Override
    public String getClassName() {
        return conf.getClassCanonicalName();
    }

    @Override
    public String getName() {
        return conf.getName();
    }

    @Override
    public Properties getProperties() {
        var r = new Properties();
        r.putAll(conf.getProperties());
        return r;
    }

    @Override
    public synchronized ISessionFactory getSessionFactory() throws ConnectorException {
        if (factory == null) {
            factory = new SessionFactory(ctx);
        }
        return factory;
    }

    IConnector getConnector() throws ConnectorException {
        Objects.requireNonNull(conn);
        return conn;
    }
}
