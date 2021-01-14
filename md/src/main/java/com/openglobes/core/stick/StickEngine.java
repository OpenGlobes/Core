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
import com.openglobes.core.event.EventSourceException;
import com.openglobes.core.event.IEventSource;
import com.openglobes.core.market.InstrumentMinuteNotice;
import com.openglobes.core.market.InstrumentNotice;
import com.openglobes.core.market.Notices;
import com.openglobes.core.market.Stick;
import com.openglobes.core.market.Tick;
import com.openglobes.core.utils.Loggers;
import java.lang.ref.Cleaner;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class StickEngine implements IStickEngine, AutoCloseable {

    public static IStickEngine create(IMarketDataSource source) throws StickException,
                                                                       MarketDataSourceException {
        return new StickEngine(source);
    }
    private final Map<String, IStickBuilder> builders;
    private final Cleaner.Cleanable cleanable;
    private final Cleaner cleaner = Cleaner.create();
    private final EventSource evt;
    private final AtomicLong sid;
    private final IMarketDataSource src;

    private StickEngine(IMarketDataSource source) throws StickException,
                                                         MarketDataSourceException {
        evt = new EventSource();
        src = source;
        builders = new ConcurrentHashMap<>(512);
        sid = new AtomicLong(getInitStickId());
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
    public Long nextStickId() {
        return sid.incrementAndGet();
    }

    @Override
    public void onNotice(InstrumentMinuteNotice notice) throws StickException {
        buildAndPublish(notice.getInstrumentId(),
                        notice.getAlignTime(),
                        notice.getMinuteOfTradingDay(),
                        null);
    }

    @Override
    public void onNotice(InstrumentNotice notice) throws StickException {
        if (Notices.INSTRUMENT_END_TRADE == notice.getType()) {
            /*
             * Publish day sticks.
             */
            buildAndPublish(notice.getInstrumentId(),
                            notice.getAlignTime(),
                            null,
                            1);
            /*
             * Publish sticks of other not-yet published minutes.
             */
            buildAndPublishOthers(notice);
        }
    }

    @Override
    public void updateTick(Tick tick) throws StickException {
        getBuilder(tick.getInstrumentId()).update(tick);
    }

    private void buildAndPublish(String instrumentId,
                                 ZonedDateTime alignTime,
                                 Integer minutes,
                                 Integer days) throws StickException {
        publishSticks(getBuilder(instrumentId).build(minutes,
                                                     days,
                                                     alignTime));
    }

    private void buildAndPublishOthers(InstrumentNotice notice) throws StickException {
        publishSticks(getBuilder(notice.getInstrumentId()).tryBuild(notice.getAlignTime()));
    }

    private IStickBuilder getBuilder(String instrumentId) throws StickException {
        var b = builders.get(instrumentId);
        if (b == null) {
            throw new StickException(ErrorCode.STICKBUILDER_NOT_FOUND.code(),
                                     ErrorCode.STICKBUILDER_NOT_FOUND.message());
        }
        return b;
    }

    private long getInitStickId() {
        var c = ZonedDateTime.now();
        return (c.getYear() * 10000 + c.getMonthValue() * 100 + c.getDayOfMonth()) * 1000000;
    }

    private void publishSticks(Collection<Stick> ss) {
        ss.forEach(s -> {
            try {
                evt.publish(Stick.class, s);
            }
            catch (EventSourceException ex) {
                Loggers.getLogger(StickEngine.class.getCanonicalName()).log(Level.SEVERE,
                                                                            ex.toString(),
                                                                            ex);
            }
        });
    }

    private void setup() throws StickException,
                                MarketDataSourceException {
        try {
            var conn = src.getConnection();
            for (var setting : conn.getInstrumentStickSettings()) {
                var b = builders.computeIfAbsent(setting.getInstrumentId(),
                                             k -> {
                                                 return new StickBuilder(this);
                                             });
                b.addMinutes(setting.getMinutes());
            }
            for (var b : builders.values()) {
                b.addDays(1);
            }
        }
        catch (Exception ex) {
            throw new MarketDataSourceException(ErrorCode.DATASOURCE_AUTOCLOSE_FAIL.code(),
                                                ErrorCode.DATASOURCE_AUTOCLOSE_FAIL.message(),
                                                ex);
        }
    }

    private static class CleanAction implements Runnable {

        private final EventSource src;

        CleanAction(EventSource source) {
            src = source;
        }

        @Override
        public void run() {
            try {
                src.close();
            }
            catch (EventSourceException ex) {
                Loggers.getLogger(StickEngine.class.getCanonicalName()).log(Level.SEVERE,
                                                                            ex.toString(),
                                                                            ex);
            }
        }
    }
}
