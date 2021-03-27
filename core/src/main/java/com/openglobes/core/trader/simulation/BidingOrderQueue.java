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

import com.openglobes.core.trader.Direction;
import com.openglobes.core.trader.Request;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class BidingOrderQueue extends AbstractOrderQueue {

    private static final long serialVersionUID = 372740219L;

    public BidingOrderQueue() {
    }

    @Override
    protected void sortBuckets() {
        sort((b0, b1) -> Double.compare(b1.getPrice(), b0.getPrice()));
    }

    @Override
    protected void checkOffset(Request request) {
        if (request.getDirection() != Direction.BUY) {
            throw new IllegalArgumentException("Biding request must BUY.");
        }
    }

}
