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
package com.openglobes.core;

import com.openglobes.core.connector.IConnector;
import com.openglobes.core.connector.IConnectorContext;
import com.openglobes.core.context.IGatewayContext;
import com.openglobes.core.data.ITraderDataSource;
import com.openglobes.core.plugin.IPlugin;
import com.openglobes.core.plugin.IPluginContext;
import com.openglobes.core.session.AcquireInformationException;
import com.openglobes.core.trader.ITraderEngine;
import com.openglobes.core.trader.ITraderGateway;
import java.util.Collection;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public interface ICore {

    void start() throws CoreStartException;

    void dispose() throws CoreDisposeException;

    void installPlugin(IPlugin plugin) throws CoreInstallException;

    void installGateway(ITraderGateway gateway) throws CoreInstallException;

    void installDataSource(ITraderDataSource dataSource) throws CoreInstallException;

    IConnectorContext getConnectorContext(IConnector connector) throws AcquireInformationException;

    Collection<IConnectorContext> connectors();

    Collection<IPluginContext> plugins();

    Collection<IGatewayContext> gateways();

    IRequestContext getRequest();

    ISharedContext getShared();

    ITraderEngine getTraderEngine();
}
