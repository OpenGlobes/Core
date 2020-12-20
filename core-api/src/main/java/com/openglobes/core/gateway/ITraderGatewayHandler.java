/*
 * Copyright (C) 2020 Hongbao Chen <chenhongbao@outlook.com>
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
package com.openglobes.core.gateway;

import com.openglobes.core.exceptions.GatewayRuntimeException;
import com.openglobes.core.Request;
import com.openglobes.core.Response;
import com.openglobes.core.Trade;

/**
 * Handler for trading responses from {@link ITraderService}.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface ITraderGatewayHandler {

    void onTrade(Trade trade);

    void onResponse(Response response);

    void onException(GatewayRuntimeException exception);

    void onException(Request request, GatewayRuntimeException exception, int requestId);

    void onStatusChange(int status);
}
