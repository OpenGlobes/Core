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

import com.openglobes.core.data.IMarketData;
import com.openglobes.core.data.MarketDataSourceException;
import com.openglobes.core.event.EventSource;
import com.openglobes.core.event.EventSourceException;
import com.openglobes.core.event.IEvent;
import com.openglobes.core.event.IEventHandler;
import com.openglobes.core.event.IEventSource;
import com.openglobes.core.market.InstrumentMinuteNotice;
import com.openglobes.core.market.InstrumentNotice;
import com.openglobes.core.market.InstrumentTime;
import com.openglobes.core.market.Notices;
import com.openglobes.core.utils.Loggers;
import com.openglobes.core.utils.MinuteNotice;
import com.openglobes.core.utils.Utils;
import java.lang.ref.Cleaner;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class InstrumentNotifier implements IEventHandler<MinuteNotice>,
                                           IInstrumentNotifier,
                                           AutoCloseable {

    public static InstrumentNotifier create(IMarketData connection) throws MarketDataSourceException {
        return new InstrumentNotifier(connection);
    }

    private final Cleaner.Cleanable cleanable;
    private final Cleaner cleaner = Cleaner.create();
    private final IMarketData conn;
    private final IEventSource evt;
    private final Map<String, AtomicInteger> minCounters;
    private final Map<String, Integer> preTypes;
    private final Map<TimeKeeper, Set<String>> times;

    private InstrumentNotifier(IMarketData connection) throws MarketDataSourceException {
        conn = connection;
        evt = new EventSource();
        times = new HashMap<>(512);
        preTypes = new HashMap<>(512);
        minCounters = new HashMap<>(512);
        cleanable = cleaner.register(this,
                                     new CleanAction(evt));
        setup();
    }

    @Override
    public void close() throws Exception {
        cleanable.clean();
    }

    @Override
    public IEventSource getEventSource() {
        return evt;
    }

    @Override
    public void handle(IEvent<MinuteNotice> event) {
        try {
            var min = event.get();
            var day = conn.getTradingDay();
            times.forEach((keeper, instruments) -> {
                var type = getType(min.getAlignTime(),
                               keeper);
                instruments.forEach(instrumentId -> {
                    var pre = preTypes.getOrDefault(instrumentId,
                                                Notices.INSTRUMENT_NO_TRADE);
                    if (!Objects.equals(pre, type)) {
                        preTypes.put(instrumentId, type);
                        sendInstrumentNotice(instrumentId,
                                             type,
                                             min,
                                             day.getTradingDay());
                        if (Notices.INSTRUMENT_TRADE == pre) {
                            sendInstrumentMinuteNotice(instrumentId,
                                                       min,
                                                       day.getTradingDay());
                        }
                        if (Notices.INSTRUMENT_END_TRADE == type) {
                            resetEndOfTrade();
                        }
                    }
                    else {
                        if (Notices.INSTRUMENT_TRADE == type) {
                            sendInstrumentMinuteNotice(instrumentId,
                                                       min,
                                                       day.getTradingDay());
                        }
                    }
                });
            });
        }
        catch (MarketDataSourceException ex) {
            Loggers.getLogger(InstrumentNotifier.class.getCanonicalName()).log(Level.SEVERE,
                                                                               ex.toString(),
                                                                               ex);
        }
    }

    @Override
    public void reload() throws MarketDataSourceException {
        times.clear();
        setupTimes(conn.getInstrumentTimes());
    }

    private Integer getMinuteOfTradingDay(String instrumentId) {
        synchronized (minCounters) {
            return minCounters.get(instrumentId).incrementAndGet();
        }
    }

    private Integer getMinutes() {
        return 1;
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

    private void setupCounters(Collection<InstrumentTime> instruments) {
        instruments.forEach(time -> {
            minCounters.put(time.getInstrumentId(),
                            new AtomicInteger(0));
        });
    }

    private void setupTimes(Collection<InstrumentTime> instruments) {
        instruments.forEach(time -> {
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

    private void resetEndOfTrade() {
        synchronized (minCounters) {
            minCounters.entrySet().forEach(entry -> {
                entry.getValue().set(0);
            });
        }
    }

    private void sendInstrumentMinuteNotice(String instrumentId,
                                            MinuteNotice min,
                                            LocalDate tradingDay) {
        try {
            evt.publish(InstrumentMinuteNotice.class,
                        new InstrumentMinuteNotice(Utils.nextId(),
                                                   instrumentId,
                                                   getMinutes(),
                                                   getMinuteOfTradingDay(instrumentId),
                                                   min.getAlignTime(),
                                                   ZonedDateTime.now(),
                                                   tradingDay));
        }
        catch (EventSourceException ex) {
            Loggers.getLogger(InstrumentNotifier.class.getCanonicalName()).log(Level.SEVERE,
                                                                               ex.toString(),
                                                                               ex);
        }
    }

    private void sendInstrumentNotice(String instrumentId,
                                      Integer type,
                                      MinuteNotice notice,
                                      LocalDate tradingDay) {
        try {
            evt.publish(InstrumentNotice.class,
                        new InstrumentNotice(Utils.nextId(),
                                             instrumentId,
                                             type,
                                             notice.getAlignTime(),
                                             ZonedDateTime.now(),
                                             tradingDay));
        }
        catch (EventSourceException ex) {
            Loggers.getLogger(InstrumentNotifier.class.getCanonicalName()).log(Level.SEVERE,
                                                                               ex.toString(),
                                                                               ex);
        }
    }

    private void setup() throws MarketDataSourceException {
        var instruments = conn.getInstrumentTimes();
        setupTimes(instruments);
        setupCounters(instruments);
    }

    private static class CleanAction implements Runnable {

        private final IEventSource src;

        CleanAction(IEventSource source) {
            src = source;
        }

        @Override
        public void run() {
            src.close();
        }
    }
}
