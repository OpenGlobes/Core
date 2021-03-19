/*
 * Copyright (C) 2021 Hongbao Chen <chenhongbao@outlook.com>
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
package com.openglobes.core.interceptor;

import com.openglobes.core.IRequestContext;
import com.openglobes.core.utils.Loggers;
import java.util.logging.Level;

/**
 * @author chenh
 */
public class LastRequestInterceptor extends AbstractRequestInterceptor<RequestInterceptingContext> {

    private final IRequestContext ctx;

    public LastRequestInterceptor(IRequestContext context) {
        ctx = context;
    }

    @Override
    public InterceptOperation onRequest(RequestInterceptingContext context,
                                        IInterceptorChain stack) {
        try {
            ctx.getTraderEngine().request(context.getRequest(),
                                          context.getInstrument(),
                                          context.getProperties(),
                                          context.getRequestId());
        } catch (Throwable th) {
            Loggers.getLogger(LastRequestInterceptor.class.getCanonicalName())
                    .log(Level.SEVERE, th.toString(), th);
        }
        return InterceptOperation.SKIP_REST;
    }

}
