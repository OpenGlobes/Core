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

import com.openglobes.core.ISharedContext;
import com.openglobes.core.session.SessionException;
import com.openglobes.core.trader.EngineRequestError;
import com.openglobes.core.utils.Loggers;

import java.util.logging.Level;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class LastRequestErrorInterceptor extends AbstractResponseInterceptor<EngineRequestError> {

    private final ISharedContext ctx;

    public LastRequestErrorInterceptor(ISharedContext context) {
        ctx = context;
    }

    @Override
    public InterceptOperation onResponse(EngineRequestError response, IInterceptorChain stack) {
        try {
            ctx.getSessionCorrelator()
               .getSessionByOrderId(response.getRequest().getOrderId())
               .respond(response);
        } catch (SessionException ex) {
            Loggers.getLogger(LastRequestErrorInterceptor.class.getCanonicalName()).log(Level.SEVERE,
                                                                                        ex.toString(),
                                                                                        ex);
        }
        return InterceptOperation.SKIP_REST;
    }

}
