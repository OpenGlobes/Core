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
package com.openglobes.core.session;

import com.openglobes.core.IRequestContext;
import com.openglobes.core.RequestException;
import com.openglobes.core.connector.IConnector;
import com.openglobes.core.context.ResponseContext;

import java.util.Objects;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class SessionFactory implements ISessionFactory {

    private final IRequestContext context;

    public SessionFactory(IRequestContext context) {
        this.context = context;
    }

    @Override
    public ISession createSession(IConnector connector) throws AcquireInformationException {
        Objects.requireNonNull(connector);
        try {
            var shared = context.getSharedContext();
            return new Session(context,
                               new ResponseContext(connector, shared));
        } catch (RequestException ex) {
            throw new AcquireInformationException(ex.getMessage(),
                                                  ex);
        }
    }

}
