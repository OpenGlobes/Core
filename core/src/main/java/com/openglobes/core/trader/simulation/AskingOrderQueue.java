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

import com.openglobes.core.trader.IllegalRequestException;
import com.openglobes.core.trader.Request;
import java.util.LinkedList;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class AskingOrderQueue extends LinkedList<RequestBucket> implements IOrderQueue{

    private static final long serialVersionUID = 127649287430L;

    @Override
    public Object clone() {
        return super.clone();
    }

    @Override
    public void dequeOrder(Long orderId) throws UnkownOrderIdException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void enqueOrder(Request request) throws IllegalRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
