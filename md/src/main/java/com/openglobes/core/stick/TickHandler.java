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
package com.openglobes.core.stick;

import com.openglobes.core.GatewayRuntimeException;
import com.openglobes.core.ServiceRuntimeStatus;
import com.openglobes.core.market.Tick;
import com.openglobes.core.utils.Loggers;

import java.util.Objects;
import java.util.logging.Level;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class TickHandler implements IMarketGatewayHandler {

    private final IStickEngine eg;

    public TickHandler(IStickEngine generator) {
        Objects.requireNonNull(generator);
        eg = generator;
    }

    @Override
    public void onException(GatewayRuntimeException ex) {
        Loggers.getLogger(TickHandler.class.getCanonicalName()).log(Level.SEVERE,
                                                                    ex.getMessage(),
                                                                    ex);
    }

    @Override
    public void onStatusChange(ServiceRuntimeStatus status) {
        Loggers.getLogger(TickHandler.class.getCanonicalName()).log(Level.INFO,
                                                                    status.toString(),
                                                                    status);
    }

    @Override
    public void onTick(Tick tick) {
        try {
            eg.updateTick(tick);
        } catch (StickBuilderNotFoundException | StickException ex) {
            Loggers.getLogger(TickHandler.class.getCanonicalName()).log(Level.SEVERE,
                                                                        ex.getMessage(),
                                                                        ex);
        }
    }

}
