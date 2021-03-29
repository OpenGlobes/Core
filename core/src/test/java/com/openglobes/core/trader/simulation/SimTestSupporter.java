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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

public class SimTestSupporter {
    private final Map<Long, ResponsePack> responses = new HashMap<>();

    private ResponsePack pack(Long orderId) {
        return responses.computeIfAbsent(orderId, id -> new ResponsePack());
    }

    protected List<Trade> trades(Long orderId) {
        return pack(orderId).trades;
    }

    protected List<Response> goodResponses(Long orderId) {
        return pack(orderId).goodResponses;
    }

    protected List<Response> badResponses(Long orderId) {
        return pack(orderId).badResponses;
    }

    protected void waitResponse() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}
