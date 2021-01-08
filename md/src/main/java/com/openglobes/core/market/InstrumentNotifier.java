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
package com.openglobes.core.market;

import com.openglobes.core.data.IMarketData;
import com.openglobes.core.data.MarketDataSourceException;
import com.openglobes.core.event.EventSource;
import com.openglobes.core.event.IEvent;
import com.openglobes.core.event.IEventHandler;
import com.openglobes.core.event.IEventSource;
import com.openglobes.core.utils.Loggers;
import com.openglobes.core.utils.MinuteNotice;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class InstrumentNotifier implements IEventHandler<MinuteNotice> {

    public static InstrumentNotifier create(IMarketData connection) throws MarketDataSourceException {
        return new InstrumentNotifier(connection);
    }

    private final IMarketData conn;
    private final IEventSource evt;
    private final Map<String, Integer> pretypes;
    private final Map<TimeKeeper, Set<String>> times;

    private InstrumentNotifier(IMarketData connection) throws MarketDataSourceException {
        conn = connection;
        evt = new EventSource();
        times = new HashMap<>(512);
        pretypes = new HashMap<>(512);
        setup();
    }

    public IEventSource getEventSource() {
        return evt;
    }

    @Override
    public void handle(IEvent<MinuteNotice> event) {
        try {
            var min = event.get();
            var n = ZonedDateTime.now();
            var day = conn.getTradingDay();
            times.forEach((keeper, instruments) -> {
                var type = getType(n, keeper);
                instruments.forEach(i -> {
                    var pre = pretypes.getOrDefault(i, Notices.INSTRUMENT_END_TRADE);
                    processInstrumentNotice(i,
                                            min,
                                            pre,
                                            type,
                                            day.getTradingDay());

                    // TODO reset minute counter and other internal data at the
                    //      end of day.
                    // TODO send instrument minute notice if it is trading or
                    //      the end of a trading sector.
                });
            });
        }
        catch (MarketDataSourceException ex) {
            Loggers.getLogger(InstrumentNotifier.class.getCanonicalName()).log(Level.SEVERE,
                                                                               ex.toString(),
                                                                               ex);
        }
    }

    private Integer getType(ZonedDateTime now,
                            TimeKeeper keeper) {
        if (keeper.isBegin(now)) {
            return Notices.INSTRUMENT_BEGIN_TRADE;
        }
        else if (keeper.isEnd(now)) {
            return Notices.INSTRUMENT_END_TRADE;
        }
        else if (keeper.isWorking(now)) {
            return Notices.INSTRUMENT_TRADE;
        }
        else {
            return Notices.INSTRUMENT_NO_TRADE;
        }
    }

    private void processInstrumentNotice(String instrument,
                                         MinuteNotice min,
                                         Integer pre,
                                         Integer type,
                                         LocalDate tradingDay) {
        if (!Objects.equals(pre, type)) {
            pretypes.put(instrument, type);
            sendInstrumentNotice(instrument,
                                 type,
                                 min,
                                 tradingDay);
        }
    }

    private void sendInstrumentNotice(String instrument,
                                      Integer type,
                                      MinuteNotice notice,
                                      LocalDate tradingDay) {
        // TODO send instrument notice
    }

    private void setup() throws MarketDataSourceException {
        conn.getInstrumentTimes().forEach(time -> {
            try {
                var set = times.computeIfAbsent(TimeKeeper.create(time.getWorkdayTimeSetId(),
                                                              time.getHolidayTimeSetId(),
                                                              conn),
                                            key -> {
                                                return new HashSet<>(12);
                                            });
                set.add(time.getInstrumentId());
            }
            catch (MarketDataSourceException ex) {
                Loggers.getLogger(InstrumentNotifier.class.getCanonicalName()).log(Level.SEVERE,
                                                                                   ex.toString(),
                                                                                   ex);
            }
        });
    }
}
