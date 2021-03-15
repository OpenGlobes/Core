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

import com.openglobes.core.connector.ConnectorException;
import com.openglobes.core.connector.IConnector;
import com.openglobes.core.connector.IConnectorContext;
import com.openglobes.core.session.ISession;

import java.util.Objects;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class ConnectorContext implements IConnectorContext {

    private final IConnector conn;
    private final ISession session;

    public ConnectorContext(IConnector connector,
                            ISession session) {
        this.conn = connector;
        this.session = session;
    }

    @Override
    public IConnector get() {
        return conn;
    }

    @Override
    public ISession getSession() {
        return session;
    }

    IConnector getConnector() throws ConnectorException {
        Objects.requireNonNull(conn);
        return conn;
    }
}
