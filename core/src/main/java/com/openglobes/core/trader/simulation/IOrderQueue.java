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

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IOrderQueue {
    void enqueOrder(Request request) throws IllegalRequestException;
    
    void dequeOrder(Long orderId) throws UnkownOrderIdException;
}
