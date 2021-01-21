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
package com.openglobes.core.event;

import com.openglobes.core.trader.Request;
import com.openglobes.core.trader.Response;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventSourceTest {

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    private final Long sleepMilli = 100L;
    private final Integer total = 100;

    public EventSourceTest() {
    }

    @Test
    @Order(1000)
    @DisplayName("Invalid subscription.")
    public void invalidSubscription() {
        final EventSource source = new EventSource();
        assertThrows(InvalidSubscriptionException.class,
                     () -> {
                         source.subscribe(Request.class,
                                          evt -> {
                                          });
                         source.subscribe(Request.class,
                                          evt -> {
                                          });
                     },
                     "Duplicated subscription shuld throw exception.");
    }

    @Test
    @Order(4)
    @DisplayName("Multiple producers, multiple consumers.")
    public void multiProducerMultiConsumer() {
        try (EventSource source = new EventSource()) {
            final AtomicLong seq0 = new AtomicLong(0);
            final AtomicLong seq1 = new AtomicLong(0);
            final var req = new SingleEventHandler<Request>();
            final var rsp = new SingleEventHandler<Response>();
            try {
                source.subscribe(Request.class,
                                 req);
                source.subscribe(Response.class,
                                 rsp);
            }
            catch (InvalidSubscriptionException ex) {
                fail(ex.getMessage());
            }
            catch (Throwable th) {
                fail(th.getMessage());
            }
            /*
             * Start event source.
             */
            source.start();
            /*
             * Publish events.
             */
            int count = 0;
            while (++count <= total) {
                var r = new Request();
                r.setOrderId(seq0.incrementAndGet());
                r.setInstrumentId("a0");
                source.publish(Request.class,
                               r);
                var rp = new Response();
                rp.setOrderId(seq1.incrementAndGet());
                rp.setInstrumentId("b0");
                source.publish(Response.class,
                               rp);
            }
            /*
             * Wait one second.
             */
            try {
                Thread.sleep(sleepMilli);
                assertEquals(total.intValue(),
                             req.getCount());
                assertEquals(total.intValue(),
                             rsp.getCount());
            }
            catch (InterruptedException ex) {
                fail(ex.getMessage());
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("Multiple producers, single consumer.")
    public void multiProducerSingleConsumer() {
        try (EventSource source = new EventSource()) {
            final ExecutorService es = Executors.newCachedThreadPool();
            final var req = new SingleEventHandler<Request>();
            final Integer threadCount = 10;
            try {
                source.subscribe(Request.class,
                                 req);
            }
            catch (InvalidSubscriptionException ex) {
                fail(ex.getMessage());
            }
            catch (Throwable th) {
                fail(th.getMessage());
            }
            /*
             * Start event source.
             */
            source.start();
            /*
             * Publish events.
             */
            for (int i = 0; i < threadCount; ++i) {
                final var offset = i;
                es.submit(() -> {
                    final AtomicLong sequence = new AtomicLong(0);
                    while (sequence.incrementAndGet() <= total) {
                        var r = new Request();
                        r.setOrderId(sequence.get());
                        r.setInstrumentId("m" + offset);
                        source.publish(Request.class,
                                       r);
                    }
                });
            }
            /*
             * Wait one second.
             */
            try {
                Thread.sleep(sleepMilli);
                assertEquals(total * threadCount,
                             req.getCount());
            }
            catch (InterruptedException ex) {
                fail(ex.getMessage());
            }
        }
    }

    @BeforeEach
    public void setUp() {
    }

    @Test
    @Order(3)
    @DisplayName("Single producer, multiple consumers.")
    public void singleProducerMultiConsumer() {
        try (EventSource source = new EventSource()) {
            final AtomicLong seq0 = new AtomicLong(0);
            final AtomicLong seq1 = new AtomicLong(0);
            final var req = new SingleEventHandler<Request>();
            final var rsp = new SingleEventHandler<Response>();
            try {
                source.subscribe(Request.class,
                                 req);
                source.subscribe(Response.class,
                                 rsp);
            }
            catch (InvalidSubscriptionException ex) {
                fail(ex.getMessage());
            }
            catch (Throwable th) {
                fail(th.getMessage());
            }
            /*
             * Start event source.
             */
            source.start();
            /*
             * Publish events.
             */
            int count = 0;
            while (++count <= total) {
                var r = new Request();
                r.setOrderId(seq0.incrementAndGet());
                r.setInstrumentId("a0");
                source.publish(Request.class,
                               r);
                var rp = new Response();
                rp.setOrderId(seq1.incrementAndGet());
                rp.setInstrumentId("b0");
                source.publish(Response.class,
                               rp);
            }
            /*
             * Wait one second.
             */
            try {
                Thread.sleep(sleepMilli);
                assertEquals(total.intValue(),
                             req.getCount());
                assertEquals(total.intValue(),
                             rsp.getCount());
            }
            catch (InterruptedException ex) {
                fail(ex.getMessage());
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("Single producer, multiple consumers.")
    public void singleProducerSingleConsumer() {
        try (EventSource source = new EventSource()) {
            final AtomicLong sequence = new AtomicLong(0);
            final var req = new SingleEventHandler<Request>();
            try {
                source.subscribe(Request.class,
                                 req);
            }
            catch (InvalidSubscriptionException ex) {
                fail(ex.getMessage());
            }
            catch (Throwable th) {
                fail(th.getMessage());
            }
            /*
             * Start event source.
             */
            source.start();
            /*
             * Publish events.
             */
            int count = 0;
            while (++count <= total) {
                var r = new Request();
                r.setOrderId(sequence.incrementAndGet());
                r.setInstrumentId("a0");
                source.publish(Request.class,
                               r);
            }
            /*
             * Wait one second.
             */
            try {
                Thread.sleep(sleepMilli);
                assertEquals(total,
                             req.getCount());
            }
            catch (InterruptedException ex) {
                fail(ex.getMessage());
            }
        }
    }

    @AfterEach
    public void tearDown() {
    }

    private class SingleEventHandler<T> implements IEventHandler<T> {

        private final AtomicInteger count = new AtomicInteger(0);
        private final Map<String, AtomicLong> m;

        SingleEventHandler() {
            this.m = new ConcurrentHashMap<>(64);
        }

        public int getCount() {
            return count.get();
        }

        @Override
        public void handle(IEvent<T> event) {
            var r = event.get();
            String instrumentId;
            Long orderId;
            if (r instanceof Request) {
                instrumentId = ((Request) r).getInstrumentId();
                orderId = ((Request) r).getOrderId();
            }
            else if (r instanceof Response) {
                instrumentId = ((Response) r).getInstrumentId();
                orderId = ((Response) r).getOrderId();
            }
            else {
                return;
            }
            var v = m.computeIfAbsent(instrumentId,
                                  k -> {
                                      return new AtomicLong(0);
                                  });
            assertEquals(v.get() + 1,
                         orderId);
            v.set(orderId);
            count.incrementAndGet();
        }
    }
}
