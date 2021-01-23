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

import com.openglobes.core.IResponseContext;
import com.openglobes.core.ISharedContext;
import com.openglobes.core.ResponseException;
import com.openglobes.core.connector.IConnector;
import com.openglobes.core.session.ISessionCorrelator;

import java.util.Objects;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class ResponseContext implements IResponseContext {

    private final IConnector     conn;
    private final ISharedContext shared;

    public ResponseContext(IConnector connector, ISharedContext context) {
        conn   = connector;
        shared = context;
    }

    @Override
    public IConnector getConnector() throws ResponseException {
        Objects.requireNonNull(conn);
        return conn;
    }

    @Override
    public ISessionCorrelator getSessionCorrelator() throws ResponseException {
        Objects.requireNonNull(shared);
        return shared.getSessionCorrelator();
    }

    @Override
    public ISharedContext getSharedContext() throws ResponseException {
        Objects.requireNonNull(shared);
        return shared;
    }
}
