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

import com.openglobes.core.configuration.*;
import com.openglobes.core.connector.ConnectorException;
import com.openglobes.core.connector.IConnector;
import com.openglobes.core.connector.IConnectorContext;
import com.openglobes.core.context.*;
import com.openglobes.core.data.ITraderDataSource;
import com.openglobes.core.interceptor.*;
import com.openglobes.core.plugin.IPlugin;
import com.openglobes.core.plugin.IPluginContext;
import com.openglobes.core.plugin.PluginException;
import com.openglobes.core.trader.*;
import com.openglobes.core.utils.ServiceSelector;

import javax.management.ServiceNotFoundException;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class Core implements ICore {

    private final ITraderEngineAlgorithm algo;
    private final ITraderEngine engine;
    private final IRequestContext reqCtx;
    private final ISharedContext sharedCtx;
    private Collection<IConnectorContext> connectors;
    private IDataSourceContext ds;
    private Collection<IGatewayContext> gates;
    private Collection<IPluginContext> plugins;

    public Core() {
        sharedCtx = new SharedContext();
        engine = new TraderEngine();
        reqCtx = new RequestContext(engine, sharedCtx);
        algo = new DefaultTraderEngineAlgorithm();
    }

    public static ICore create() {
        return new Core();
    }

    @Override
    public Collection<IConnectorContext> connectors() {
        return connectors;
    }

    @Override
    public void dispose() throws CoreDisposeException {
        try {
            engine.dispose();
            for (var c : connectors) {
                c.get().dispose();
            }
            for (var p : plugins) {
                p.get().dispose();
            }
            while (sharedCtx.getInterceptorChain().removeInterceptor() != null) {
            }
        } catch (TraderException | ConnectorException | PluginException | InterceptorException ex) {
            throw new CoreDisposeException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public Collection<IGatewayContext> gateways() {
        return gates;
    }

    @Override
    public IDataSourceContext getDataSource() {
        return ds;
    }

    @Override
    public IRequestContext getRequest() {
        return reqCtx;
    }

    @Override
    public ISharedContext getShared() {
        return sharedCtx;
    }

    @Override
    public ITraderEngine getTraderEngine() {
        return engine;
    }

    @Override
    public void install(String xml, File... jars) throws CoreInstallException {
        try {
            install(XmlConfiguration.load(CoreConfiguration.class, xml),
                    jars);
        } catch (ConfigurationException ex) {
            throw new CoreInstallException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public void install(InputStream stream, File... jars) throws CoreInstallException {
        try {
            install(XmlConfiguration.load(CoreConfiguration.class, stream),
                    jars);
        } catch (ConfigurationException ex) {
            throw new CoreInstallException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public void install(FileReader reader, File... jars) throws CoreInstallException {
        try {
            install(XmlConfiguration.load(CoreConfiguration.class, reader),
                    jars);
        } catch (ConfigurationException ex) {
            throw new CoreInstallException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public void installConnector(IConnectorContext connectorContext) throws CoreInstallException {
        Objects.requireNonNull(connectorContext);
        connectors.add(connectorContext);
        try {
            connectorContext.get().listen(connectorContext);
        } catch (ConnectorException ex) {
            throw new CoreInstallException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public void installDataSource(IDataSourceContext dataSourceContext) throws CoreInstallException {
        Objects.requireNonNull(dataSourceContext);
        ds = dataSourceContext;
    }

    @Override
    public void installGateway(IGatewayContext gatewayContext) throws CoreInstallException {
        Objects.requireNonNull(gatewayContext);
        gates.add(gatewayContext);
        try {
            engine.setDataSource(ds.get());
            engine.setAlgorithm(algo);
            engine.registerTrader(gates.size(),
                                  gatewayContext.get());
        } catch (TraderException ex) {
            throw new CoreInstallException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public void installPlugin(IPluginContext pluginContext) throws CoreInstallException {
        Objects.requireNonNull(pluginContext);
        plugins.add(pluginContext);
        try {
            pluginContext.get().initialize(pluginContext);
        } catch (PluginException ex) {
            throw new CoreInstallException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public Collection<IPluginContext> plugins() {
        return plugins;
    }

    @Override
    public void start() throws CoreStartException {
        try {
            installInterceptors();
            engine.start(new Properties());
        } catch (TraderException ex) {
            throw new CoreStartException(ex.getMessage(),
                                         ex);
        }
    }

    private void install(CoreConfiguration configuration, File[] jars) throws CoreInstallException {
        installConnectors(configuration.getConnectors(),
                          jars);
        installDataSource(configuration.getDataSources(),
                          jars);
        installPlugins(configuration.getPlugins(),
                       jars);
        installGateways(configuration.getGateways(),
                        jars);
    }

    private void installConnectors(Collection<ConnectorConfiguration> confs,
                                   File[] jars) throws CoreInstallException {
        for (var c : confs) {
            try {
                var conn = ServiceSelector.selectService(IConnector.class,
                                                     c.getClassCanonicalName(),
                                                     jars);
                installConnector(new ConnectorContext(c,
                                                      conn,
                                                      reqCtx));
            } catch (ServiceNotFoundException ex) {
                throw new CoreInstallException(ex.getMessage(),
                                               ex
                );
            }
        }
    }

    private void installDataSource(Collection<DataSourceConfiguration> confs,
                                   File[] jars) throws CoreInstallException {
        for (var c : confs) {
            try {
                var d = ServiceSelector.selectService(ITraderDataSource.class,
                                                  c.getClassCanonicalName(),
                                                  jars);
                installDataSource(new DataSourceContext(c,
                                                        d));
                break;
            } catch (ServiceNotFoundException ex) {
                throw new CoreInstallException(ex.getMessage(),
                                               ex);
            }
        }
    }

    private void installGateways(Collection<GatewayConfiguration> confs,
                                 File[] jars) throws CoreInstallException {
        for (var c : confs) {
            try {
                var gate = ServiceSelector.selectService(ITraderGateway.class,
                                                     c.getClassCanonicalName(),
                                                     jars);
                installGateway(new GatewayContext(c,
                                                  gate));
            } catch (ServiceNotFoundException ex) {
                throw new CoreInstallException(ex.getMessage(),
                                               ex);
            }
        }
    }

    private void installInterceptors() {
        var x = sharedCtx.getInterceptorChain();
        try {
            x.addInterceptor(-Integer.MAX_VALUE,
                             Trade.class,
                             new LastTradeInterceptor(sharedCtx));
            x.addInterceptor(-Integer.MAX_VALUE,
                             Response.class,
                             new LastResponseInterceptor(sharedCtx));
            x.addInterceptor(-Integer.MAX_VALUE,
                             EngineRequestError.class,
                             new LastRequestErrorInterceptor(sharedCtx));
            x.addInterceptor(Integer.MAX_VALUE,
                             RequestInterceptingContext.class,
                             new LastRequestInterceptor(reqCtx));
        } catch (InterceptorException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void installPlugins(Collection<PluginConfiguration> confs,
                                File[] jars) throws CoreInstallException {
        for (var c : confs) {
            try {
                var p = ServiceSelector.selectService(IPlugin.class,
                                                  c.getClassCanonicalName(),
                                                  jars);
                installPlugin(new PluginContext(c,
                                                p,
                                                this));
            } catch (ServiceNotFoundException ex) {
                throw new CoreInstallException(ex.getMessage(),
                                               ex);
            }
        }
    }
}
