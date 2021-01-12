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
import com.openglobes.core.event.EventSource;
import com.openglobes.core.event.IEventSource;
import com.openglobes.core.market.InstrumentMinuteNotice;
import com.openglobes.core.market.InstrumentNotice;
import com.openglobes.core.market.Tick;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class StickEngine implements IStickEngine {

    public static IStickEngine create(IMarketDataSource source) throws StickException,
                                                                       MarketDataSourceException {
        return new StickEngine(source);
    }
    private final Map<String, IStickBuilder> builders;
    private final IEventSource evt;
    private final IMarketDataSource src;

    private StickEngine(IMarketDataSource source) throws StickException,
                                                         MarketDataSourceException {
        evt = new EventSource();
        src = source;
        builders = new ConcurrentHashMap<>(512);
        setup();
    }

    @Override
    public IEventSource getEventSource() {
        return evt;
    }

    @Override
    public void onNotice(InstrumentMinuteNotice notice) throws StickException {
        //TODO on notice
    }

    @Override
    public void onNotice(InstrumentNotice notice) throws StickException {
        // TODO on notice
    }

    @Override
    public void updateTick(Tick tick) throws StickException {
        var b = builders.get(tick.getInstrumentId());
        if (b == null) {
            throw new StickException(ErrorCode.STICKBUILDER_NOT_FOUND.code(),
                                     ErrorCode.STICKBUILDER_NOT_FOUND.message());
        }
        b.update(tick);
    }

    private void setup() throws StickException,
                                MarketDataSourceException {
        try (var conn = src.getConnection()) {
            for (var setting : conn.getInstrumentStickSettings()) {
                var b = builders.computeIfAbsent(setting.getInstrumentId(),
                                             k -> {
                                                 return new StickBuilder();
                                             });
                b.addMinutes(setting.getMinutes());
            }
        }
        catch (Exception ex) {
            throw new MarketDataSourceException(ErrorCode.DATASOURCE_AUTOCLOSE_FAIL.code(),
                                                ErrorCode.DATASOURCE_AUTOCLOSE_FAIL.message(),
                                                ex);
        }
    }
}
