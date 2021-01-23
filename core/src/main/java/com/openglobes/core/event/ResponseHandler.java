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
package com.openglobes.core.event;

import com.openglobes.core.interceptor.IInterceptorChain;
import com.openglobes.core.interceptor.InterceptorException;
import com.openglobes.core.trader.Response;
import com.openglobes.core.utils.Loggers;

import java.util.logging.Level;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class ResponseHandler implements IEventHandler<Response> {

    private final IInterceptorChain interceptors;

    public ResponseHandler(IInterceptorChain interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public void handle(IEvent<Response> event) {
        try {
            var rsp = event.get();
            interceptors.respond(Response.class, rsp);
        } catch (InterceptorException ex) {
            Loggers.getLogger(ResponseHandler.class.getCanonicalName()).log(Level.SEVERE,
                                                                            ex.toString(),
                                                                            ex);
        }
    }

}
