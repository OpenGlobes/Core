/*
 * Copyright (c) 2020-2021. Hongbao Chen <chenhongbao@outlook.com>
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

import com.openglobes.core.GatewayRuntimeException;
import com.openglobes.core.ICoreListener;
import com.openglobes.core.utils.Loggers;

import java.util.logging.Level;

public class GatewayRuntimeExceptionHandler implements IEventHandler<GatewayRuntimeException> {

    private final ICoreListener l;

    public GatewayRuntimeExceptionHandler(ICoreListener listener) {
        l = listener;
    }

    @Override
    public void handle(IEvent<GatewayRuntimeException> event) {
        try {
            if (l != null) {
                l.onError(event.get());
            }
        } catch (Throwable th) {
            Loggers.getLogger(GatewayRuntimeExceptionHandler.class.getCanonicalName())
                   .log(Level.SEVERE, th.toString(), th);
        }
    }
}
