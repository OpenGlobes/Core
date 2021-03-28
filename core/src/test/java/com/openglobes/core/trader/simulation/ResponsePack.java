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

package com.openglobes.core.trader.simulation;

import com.openglobes.core.trader.Request;
import com.openglobes.core.trader.Response;
import com.openglobes.core.trader.Trade;

import java.util.LinkedList;
import java.util.List;

public class ResponsePack {
    public final List<Trade> trades = new LinkedList<>();
    public final List<Response> goodResponses = new LinkedList<>();
    public final List<Request> badRequests = new LinkedList<>();
    public final List<Response> badResponses = new LinkedList<>();

    public ResponsePack() {}

}
