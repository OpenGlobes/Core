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

import com.openglobes.core.connector.IConnectorContext;
import com.openglobes.core.context.IDataSourceContext;
import com.openglobes.core.context.IGatewayContext;
import com.openglobes.core.plugin.IPluginContext;
import com.openglobes.core.trader.ITraderEngine;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Collection;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public interface ICore {

    void start() throws CoreStartException;

    void dispose() throws CoreDisposeException;

    void installPlugin(IPluginContext pluginContext) throws CoreInstallException;

    void installConnector(IConnectorContext connectorContext) throws CoreInstallException;

    void installGateway(IGatewayContext gatewayContext) throws CoreInstallException;

    void installDataSource(IDataSourceContext dataSourceContext) throws CoreInstallException;

    void install(String xml, File... jars) throws CoreInstallException;

    void install(InputStream stream, File... jars) throws CoreInstallException;

    void install(FileReader reader, File... jars) throws CoreInstallException;

    Collection<IConnectorContext> connectors();

    Collection<IPluginContext> plugins();

    Collection<IGatewayContext> gateways();

    IDataSourceContext getDataSource();

    IRequestContext getRequest();

    ISharedContext getShared();

    ITraderEngine getTraderEngine();
}
