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
import com.openglobes.core.ISharedContext;
import com.openglobes.core.RequestException;
import com.openglobes.core.session.ISessionCorrelator;
import com.openglobes.core.trader.ITraderEngine;

import java.util.Objects;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class RequestContext implements IRequestContext {

    private final ITraderEngine eg;
    private final ISharedContext shared;

    public RequestContext(ITraderEngine engine, ISharedContext context) {
        shared = context;
        eg = engine;
    }

    @Override
    public ISessionCorrelator getSessionCorrelator() throws RequestException {
        Objects.requireNonNull(shared);
        return shared.getSessionCorrelator();
    }

    @Override
    public ISharedContext getSharedContext() throws RequestException {
        Objects.requireNonNull(shared);
        return shared;
    }

    @Override
    public ITraderEngine getTraderEngine() throws RequestException {
        Objects.requireNonNull(eg);
        return eg;
    }
}
