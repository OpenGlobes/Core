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

import com.openglobes.core.ISharedContext;
import com.openglobes.core.interceptor.IInterceptorChain;
import com.openglobes.core.interceptor.InterceptorChain;
import com.openglobes.core.session.SessionCorrelator;
import com.openglobes.core.utils.Loggers;

import java.util.logging.Handler;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class SharedContext implements ISharedContext {

    private final SessionCorrelator corr = new SessionCorrelator();
    private final IInterceptorChain interceptors = new InterceptorChain();

    @Override
    public void addLogHandler(Handler handler) {
        Loggers.addLogHandler(handler);
    }

    @Override
    public IInterceptorChain getInterceptorChain() {
        return interceptors;
    }

    @Override
    public SessionCorrelator getSessionCorrelator() {
        return corr;
    }

    @Override
    public void removeLogHandler(Handler handler) {
        Loggers.removeLogHandler(handler);
    }
}
