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

import com.openglobes.core.GatewayException;
import com.openglobes.core.configuration.GatewayConfiguration;
import com.openglobes.core.data.DataException;
import com.openglobes.core.data.IMarketDataSource;
import com.openglobes.core.event.IEventSource;
import com.openglobes.core.event.InvalidSubscriptionException;
import com.openglobes.core.market.InstrumentMinuteNotice;
import com.openglobes.core.market.InstrumentNotice;
import com.openglobes.core.utils.IMinuteNotifier;
import com.openglobes.core.utils.Loggers;
import com.openglobes.core.utils.MinuteNotice;
import com.openglobes.core.utils.MinuteNotifier;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class MarketEngine implements IMarketEngine {

    private final Map<Integer, MarketGatewayContext> gates;
    private final IMinuteNotifier minNotifier;
    private IMarketDataSource ds;
    private IStickEngine eg;
    private TickHandler gateHandler;
    private InstrumentNotifier instNotifier;

    public MarketEngine() {
        gates = new ConcurrentHashMap<>(16);
        minNotifier = new MinuteNotifier();
    }

    @Override
    public void dispose() throws MarketDisposeException {
        for (var g : gates.values()) {
            try {
                g.getGateway().stop();
            } catch (GatewayException ex) {
                throw new MarketDisposeException(ex.getMessage() + "(" + ex.getCode() + ")",
                                                 ex);
            }
        }
    }

    @Override
    public void setDataSource(IMarketDataSource dataSource) {
        Objects.requireNonNull(dataSource);
        ds = dataSource;
    }

    @Override
    public IEventSource getEventSource() {
        return eg.getEventSource();
    }

    @Override
    public void installGateway(int marketId,
                               IMarketGateway gateway,
                               GatewayConfiguration configuration) throws DuplicatedMarketIdException {
        Objects.requireNonNull(gateway);
        if (gates.containsKey(marketId)) {
            throw new DuplicatedMarketIdException(Integer.toString(marketId));
        }
        var props = new Properties();
        configuration.getProperties().entrySet().forEach(entry -> {
            props.put(entry.getKey(), entry.getValue());
        });
        gates.put(marketId, new MarketGatewayContext(marketId,
                                                     gateway,
                                                     props));
    }

    @Override
    public void start(Properties properties) throws MarketStartException {
        Objects.requireNonNull(ds);
        startFacilities();
        startGateways(properties);
    }

    @Override
    public void removeGateway(int marketId) throws UnknownMarketIdException {
        if (!gates.containsKey(marketId)) {
            throw new UnknownMarketIdException(Integer.toString(marketId));
        }
        gates.remove(marketId);
    }

    private void startFacilities() throws MarketStartException {
        try {
            /*
             * Connect stick engine to ticks.
             */
            eg = StickEngine.create(ds);
            gateHandler = new TickHandler(eg);
            /*
             * Connect stick engine to notices.
             */
            instNotifier = InstrumentNotifier.create(ds);
            instNotifier.getEventSource().subscribe(InstrumentNotice.class,
                                                    new InstrumentNoticeHandler(eg));
            instNotifier.getEventSource().subscribe(InstrumentMinuteNotice.class,
                                                    new InstrumentMinuteNoticeHandler(eg));
            /*
             * Connect instrument notifier to minute notifier.
             */
            minNotifier.getEventSource().subscribe(MinuteNotice.class,
                                                   instNotifier);
        } catch (StickException | DataException ex) {
            throw new MarketStartException(ex.getMessage(),
                                           ex);
        } catch (InvalidSubscriptionException ex) {
            Loggers.getLogger(MarketEngine.class.getCanonicalName()).log(Level.SEVERE,
                                                                         ex.getMessage(),
                                                                         ex);
        }
    }

    private void startGateways(Properties properties) throws MarketStartException {
        for (var g : gates.values()) {
            try {
                var p = new Properties(properties);
                p.putAll(g.getProperties());
                g.getGateway().start(p, gateHandler);
            } catch (GatewayException ex) {
                throw new MarketStartException(ex.getMessage() + "(" + ex.getCode() + ")",
                                               ex);
            }
        }
    }

}
