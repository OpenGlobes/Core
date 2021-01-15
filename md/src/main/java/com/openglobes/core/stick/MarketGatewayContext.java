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

import java.util.Properties;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class MarketGatewayContext {
    private final IMarketGateway gateway;
    private final Integer marketId;
    private final Properties props;

    public MarketGatewayContext(Integer marketId, 
                                IMarketGateway gateway, 
                                Properties properties) {
        this.props = properties;
        this.gateway = gateway;
        this.marketId = marketId;
    }

    public IMarketGateway getGateway() {
        return gateway;
    }

    public Integer getMarketId() {
        return marketId;
    }

    public Properties getProperties() {
        return new Properties(props);
    }
}
