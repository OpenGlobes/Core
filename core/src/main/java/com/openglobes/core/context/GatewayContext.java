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
package com.openglobes.core.context;

import com.openglobes.core.configuration.GatewayConfiguration;
import com.openglobes.core.trader.ITraderGateway;

import java.util.Properties;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class GatewayContext implements IGatewayContext {

    private final GatewayConfiguration conf;
    private final ITraderGateway gate;

    public GatewayContext(GatewayConfiguration configuration,
                          ITraderGateway gateway) {
        this.conf = configuration;
        this.gate = gateway;
    }

    @Override
    public String getClassName() {
        return conf.getClassCanonicalName();
    }

    @Override
    public String getName() {
        return conf.getName();
    }

    @Override
    public ITraderGateway get() {
        return gate;
    }

    @Override
    public Properties getProperties() {
        var r = new Properties();
        r.putAll(conf.getProperties());
        return r;
    }

}
