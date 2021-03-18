/*
 * Copyright (C) 2020-2021 Hongbao Chen <chenhongbao@outlook.com>
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
package com.openglobes.core.trader.simulation;

import com.openglobes.core.GatewayException;
import com.openglobes.core.trader.ITraderGateway;
import com.openglobes.core.trader.ITraderGatewayHandler;
import com.openglobes.core.trader.Request;
import com.openglobes.core.trader.TraderGatewayInfo;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class SimulatedTraderGateway implements ITraderGateway {

    @Override
    public TraderGatewayInfo getGatewayInfo() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setHandler(ITraderGatewayHandler handler) throws GatewayException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SimulatedMarketer getMarketer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insert(Request request, long requestId) throws GatewayException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
