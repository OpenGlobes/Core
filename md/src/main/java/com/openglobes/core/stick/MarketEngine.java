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

import com.openglobes.core.data.IMarketDataSource;
import com.openglobes.core.data.MarketDataSourceException;
import com.openglobes.core.event.EventSourceException;
import com.openglobes.core.event.IEventSource;
import com.openglobes.core.exceptions.EngineException;
import com.openglobes.core.exceptions.GatewayException;
import com.openglobes.core.market.InstrumentMinuteNotice;
import com.openglobes.core.market.InstrumentNotice;
import com.openglobes.core.utils.IMinuteNotifier;
import com.openglobes.core.utils.MinuteNotice;
import com.openglobes.core.utils.MinuteNotifier;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class MarketEngine implements IMarketEngine {

    private IMarketDataSource ds;
    private IStickEngine eg;
    private TickHandler gateHandler;
    private final Map<Integer, IMarketGateway> gates;
    private InstrumentNotifier instNotifier;
    private final IMinuteNotifier minNotifier;

    public MarketEngine() {
        gates = new ConcurrentHashMap<>(16);
        minNotifier = new MinuteNotifier();
    }

    @Override
    public void setDataSource(IMarketDataSource dataSource) throws EngineException {
        Objects.requireNonNull(dataSource);
        ds = dataSource;
    }

    @Override
    public IEventSource getEventSource() throws EngineException {
        return eg.getEventSource();
    }

    @Override
    public void registerMarket(int marketId, IMarketGateway gateway) throws EngineException {
        Objects.requireNonNull(gateway);
        if (gates.containsKey(marketId)) {
            throw new EngineException(ErrorCode.GATEWAY_DUPLICATED_ID.code(),
                                      ErrorCode.GATEWAY_DUPLICATED_ID.message() + "(Market ID: " + marketId + ")");
        }
        gates.put(marketId, gateway);
    }

    @Override
    public void start(Properties properties) throws EngineException {
        Objects.requireNonNull(ds);
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
            for (var g : gates.values()) {
                try {
                    g.start(properties, gateHandler);
                }
                catch (GatewayException e) {
                    throw new EngineException(e.getCode(),
                                              e.getMessage(),
                                              e);
                }
            }
        }
        catch (StickException | MarketDataSourceException ex) {
            throw new EngineException(ex.getCode(),
                                      ex.getMessage(),
                                      ex);
        }
        catch (EventSourceException ex) {
            Logger.getLogger(MarketEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stop() throws EngineException {
        for (var g : gates.values()) {
            try {
                g.stop();
            }
            catch (GatewayException e) {
                throw new EngineException(e.getCode(),
                                          e.getMessage(),
                                          e);
            }
        }
    }

    @Override
    public void unregisterMarket(int marketId) throws EngineException {
        if (!gates.containsKey(marketId)) {
            throw new EngineException(ErrorCode.GATEWAY_ID_NOT_FOUND.code(),
                                      ErrorCode.GATEWAY_ID_NOT_FOUND.message() + "(Market ID: " + marketId + ")");
        }
        gates.remove(marketId);
    }

}
