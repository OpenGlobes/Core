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
import com.openglobes.core.context.*;
import com.openglobes.core.data.ITraderDataSource;
import com.openglobes.core.event.*;
import com.openglobes.core.interceptor.*;
import com.openglobes.core.plugin.IPlugin;
import com.openglobes.core.plugin.IPluginContext;
import com.openglobes.core.plugin.PluginException;
import com.openglobes.core.session.AcquireInformationException;
import com.openglobes.core.session.ISessionFactory;
import com.openglobes.core.session.SessionFactory;
import com.openglobes.core.trader.*;

import java.util.Collection;
import java.util.Objects;
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
    private final ISessionFactory factory;
    private Collection<IConnectorContext> connectors;
    private IDataSourceContext ds;
    private Collection<IGatewayContext> gates;
    private Collection<IPluginContext> plugins;
    private ICoreListener coreLis;
    public Core() {
        sharedCtx = new SharedContext();
        engine = new TraderEngine();
        reqCtx = new RequestContext(engine, sharedCtx);
        algo = new DefaultTraderEngineAlgorithm();
        factory = new SessionFactory(reqCtx);
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
            for (var c : connectors) {
                c.get().dispose();
            }
            for (var p : plugins) {
                p.get().dispose();
            }
            while (sharedCtx.getInterceptorChain().removeInterceptor() != null) {
            }
        } catch (ConnectorException | PluginException | InterceptorException ex) {
            throw new CoreDisposeException(ex.getMessage(),
                                           ex);
        }
    }

    @Override
    public Collection<IGatewayContext> gateways() {
        return gates;
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
    public IConnectorContext getConnectorContext(IConnector connector) throws AcquireInformationException {
        return new ConnectorContext(connector,
                                    factory.createSession(connector));
    }

    @Override
    public void installDataSource(ITraderDataSource dataSource) throws CoreInstallException {
        Objects.requireNonNull(dataSource);
        if (ds != null) {
            throw new CoreInstallException("Data source can't be reinstalled.");
        }
        ds = new DataSourceContext(dataSource);
    }

    @Override
    public void setListener(ICoreListener listener) {
        coreLis = listener;
    }

    @Override
    public void installGateway(ITraderGateway gateway) throws CoreInstallException {
        Objects.requireNonNull(gateway);
        GatewayContext gctx = new GatewayContext(gateway);
        gates.add(gctx);
        try {
            engine.setDataSource(ds.get());
            engine.setAlgorithm(algo);
            engine.registerTrader(gates.size(), gateway);
            /* Install event handlers. */
            installEngineEventHandlers(engine);
        } catch (DuplicatedTraderIdException | InvalidSubscriptionException ex) {
            throw new CoreInstallException(ex.getMessage(), ex);
        }
    }

    private void installEngineEventHandlers(ITraderEngine engine) throws InvalidSubscriptionException {
        var chain = sharedCtx.getInterceptorChain();
        var src = engine.getEventSource();
        src.subscribe(Trade.class, new TradeHandler(chain));
        src.subscribe(Response.class, new ResponseHandler(chain));
        src.subscribe(EngineRequestError.class, new RequestErrorHandler(chain));
        src.subscribe(TraderRuntimeException.class, new TraderRuntimeExceptionHandler(coreLis));
        src.subscribe(GatewayRuntimeException.class, new GatewayRuntimeExceptionHandler(coreLis));
        src.subscribe(ServiceRuntimeStatus.class, new ServiceRuntimeStatusHandler(coreLis));
    }

    @Override
    public void installPlugin(IPlugin plugin) throws CoreInstallException {
        Objects.requireNonNull(plugin);
        PluginContext pctx = new PluginContext(plugin, this);
        plugins.add(pctx);
        try {
            plugin.initialize(pctx);
        } catch (PluginException ex) {
            throw new CoreInstallException(ex.getMessage(), ex);
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
        } catch (Throwable th) {
            throw new CoreStartException(th);
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
}
