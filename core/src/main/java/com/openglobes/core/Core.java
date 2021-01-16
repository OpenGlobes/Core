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

import com.openglobes.core.connector.ConnectorException;
import com.openglobes.core.connector.IConnector;
import com.openglobes.core.connector.IConnectorContext;
import com.openglobes.core.data.ITraderDataSource;
import com.openglobes.core.exceptions.EngineException;
import com.openglobes.core.interceptor.InterceptorException;
import com.openglobes.core.plugin.IPlugin;
import com.openglobes.core.plugin.IPluginContext;
import com.openglobes.core.plugin.PluginException;
import com.openglobes.core.trader.DefaultTraderEngineAlgorithm;
import com.openglobes.core.trader.EngineRequestError;
import com.openglobes.core.trader.ITraderEngine;
import com.openglobes.core.trader.ITraderEngineAlgorithm;
import com.openglobes.core.trader.ITraderGateway;
import com.openglobes.core.trader.Response;
import com.openglobes.core.trader.Trade;
import com.openglobes.core.trader.TraderEngine;
import com.openglobes.core.utils.Loggers;
import com.openglobes.core.utils.ServiceSelector;
import com.openglobes.core.configuration.ConfigurationException;
import com.openglobes.core.configuration.ConnectorConfiguration;
import com.openglobes.core.configuration.CoreConfiguration;
import com.openglobes.core.configuration.DataSourceConfiguration;
import com.openglobes.core.configuration.GatewayConfiguration;
import com.openglobes.core.configuration.PluginConfiguration;
import com.openglobes.core.configuration.XmlConfiguration;
import com.openglobes.core.context.ConnectorContext;
import com.openglobes.core.context.DataSourceContext;
import com.openglobes.core.context.GatewayContext;
import com.openglobes.core.context.PluginContext;
import com.openglobes.core.context.RequestContext;
import com.openglobes.core.context.SharedContext;
import com.openglobes.core.interceptor.LastRequestErrorInterceptor;
import com.openglobes.core.interceptor.LastRequestInterceptor;
import com.openglobes.core.interceptor.LastResponseInterceptor;
import com.openglobes.core.interceptor.LastTradeInterceptor;
import com.openglobes.core.interceptor.RequestInterceptingContext;
import com.openglobes.core.context.IDataSourceContext;
import com.openglobes.core.context.IGatewayContext;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.ServiceNotFoundException;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class Core implements ICore {

    public static ICore create() {
        return new Core();
    }
    private final ITraderEngineAlgorithm algo;
    private Collection<IConnectorContext> connectors;
    private IDataSourceContext ds;
    private final ITraderEngine engine;
    private Collection<IGatewayContext> gates;
    private Collection<IPluginContext> plugins;
    private final ExecutorService pool;
    private final IRequestContext reqCtx;
    private final ISharedContext sharedCtx;

    public Core() {
        pool = Executors.newCachedThreadPool();
        sharedCtx = new SharedContext();
        engine = new TraderEngine();
        reqCtx = new RequestContext(engine, sharedCtx);
        algo = new DefaultTraderEngineAlgorithm();
    }

    @Override
    public Collection<IConnectorContext> connectors() {
        return connectors;
    }

    @Override
    public void dispose() throws CoreException {
        try {
            engine.dispose();
            for (var c : connectors) {
                c.get().dispose();
            }
            for (var p : plugins) {
                p.get().dispose();
            }
            while(sharedCtx.getIInterceptorStack().removeInterceptor() != null) {
            }
        }
        catch (EngineException | ConnectorException | PluginException | InterceptorException ex) {
            throw new CoreException(ex.getCode(),
                                    ex.getMessage(),
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
    public void install(String xml, File... jars) throws CoreException {
        try {
            install(XmlConfiguration.load(CoreConfiguration.class, xml),
                    jars);
        }
        catch (ConfigurationException ex) {
            throw new CoreException(ex.getCode(),
                                    ex.getMessage(),
                                    ex);
        }
    }

    @Override
    public void install(InputStream stream, File... jars) throws CoreException {
        try {
            install(XmlConfiguration.load(CoreConfiguration.class, stream),
                    jars);
        }
        catch (ConfigurationException ex) {
            throw new CoreException(ex.getCode(),
                                    ex.getMessage(),
                                    ex);
        }
    }

    @Override
    public void install(FileReader reader, File... jars) throws CoreException {
        try {
            install(XmlConfiguration.load(CoreConfiguration.class, reader),
                    jars);
        }
        catch (ConfigurationException ex) {
            throw new CoreException(ex.getCode(),
                                    ex.getMessage(),
                                    ex);
        }
    }

    @Override
    public void installConnector(IConnectorContext connectorContext) throws CoreException {
        Objects.requireNonNull(connectorContext);
        connectors.add(connectorContext);
        pool.submit(() -> {
            try {
                connectorContext.get().listen(connectorContext);
            }
            catch (ConnectorException ex) {
                Loggers.getLogger(Core.class.getCanonicalName()).log(Level.SEVERE,
                                                                     ex.toString(),
                                                                     ex);
            }
        });
    }

    @Override
    public void installDataSource(IDataSourceContext dataSourceContext) throws CoreException {
        Objects.requireNonNull(dataSourceContext);
        ds = dataSourceContext;
    }

    @Override
    public void installGateway(IGatewayContext gatewayContext) throws CoreException {
        Objects.requireNonNull(gatewayContext);
        gates.add(gatewayContext);
        try {
            engine.setDataSource(ds.get());
            engine.setAlgorithm(algo);
            engine.registerTrader(gates.size(),
                                  gatewayContext.get());
        }
        catch (EngineException ex) {
            Loggers.getLogger(Core.class.getCanonicalName()).log(Level.SEVERE,
                                                                 ex.toString(),
                                                                 ex);
        }
    }

    @Override
    public void installPlugin(IPluginContext pluginContext) throws CoreException {
        Objects.requireNonNull(pluginContext);
        plugins.add(pluginContext);
        pool.submit(() -> {
            try {
                pluginContext.get().initialize(pluginContext);
            }
            catch (PluginException ex) {
                Loggers.getLogger(Core.class.getCanonicalName()).log(Level.SEVERE,
                                                                     ex.toString(),
                                                                     ex);
            }
        });
    }

    @Override
    public Collection<IPluginContext> plugins() {
        return plugins;
    }

    @Override
    public void start() throws CoreException {
        try {
            installInterceptors();
            engine.start(new Properties());
        }
        catch (EngineException ex) {
            throw new CoreException(ex.getCode(),
                                    ex.getMessage(),
                                    ex);
        }
    }

    private void install(CoreConfiguration configuration, File[] jars) throws CoreException {
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
                                   File[] jars) throws CoreException {
        for (var c : confs) {
            try {
                var conn = ServiceSelector.selectService(IConnector.class,
                                                     c.getClassCanonicalName(),
                                                     jars);
                installConnector(new ConnectorContext(c,
                                                      conn,
                                                      reqCtx));
            }
            catch (ServiceNotFoundException ex) {
                throw new CoreException(ErrorCode.PLAYER_CONNECTOR_NOT_FOUND.code(),
                                        ErrorCode.PLAYER_CONNECTOR_NOT_FOUND.message(),
                                        ex);
            }
        }
    }

    private void installDataSource(Collection<DataSourceConfiguration> confs,
                                   File[] jars) throws CoreException {
        for (var c : confs) {
            try {
                var d = ServiceSelector.selectService(ITraderDataSource.class,
                                                  c.getClassCanonicalName(),
                                                  jars);
                installDataSource(new DataSourceContext(c,
                                                        d));
                break;
            }
            catch (ServiceNotFoundException ex) {
                throw new CoreException(ErrorCode.PLAYER_DATASOURCE_NOT_FOUND.code(),
                                        ErrorCode.PLAYER_DATASOURCE_NOT_FOUND.message(),
                                        ex);
            }
        }
    }

    private void installGateways(Collection<GatewayConfiguration> confs,
                                 File[] jars) throws CoreException {
        for (var c : confs) {
            try {
                var gate = ServiceSelector.selectService(ITraderGateway.class,
                                                     c.getClassCanonicalName(),
                                                     jars);
                installGateway(new GatewayContext(c,
                                                  gate));
            }
            catch (ServiceNotFoundException ex) {
                throw new CoreException(ErrorCode.PLAYER_GATEWAY_NOT_FOUND.code(),
                                        ErrorCode.PLAYER_GATEWAY_NOT_FOUND.message(),
                                        ex);
            }
        }
    }

    private void installInterceptors() {
        var x = sharedCtx.getIInterceptorStack();
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
        }
        catch (InterceptorException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void installPlugins(Collection<PluginConfiguration> confs,
                                File[] jars) throws CoreException {
        for (var c : confs) {
            try {
                var p = ServiceSelector.selectService(IPlugin.class,
                                                  c.getClassCanonicalName(),
                                                  jars);
                installPlugin(new PluginContext(c,
                                                p,
                                                this));
            }
            catch (ServiceNotFoundException ex) {
                throw new CoreException(ErrorCode.PLAYER_PLUGIN_NOT_FOUND.code(),
                                        ErrorCode.PLAYER_PLUGIN_NOT_FOUND.message(),
                                        ex);
            }
        }
    }
}
