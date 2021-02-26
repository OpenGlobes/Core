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
package com.openglobes.core.interceptor;

import com.openglobes.core.trader.Instrument;
import com.openglobes.core.trader.Request;
import com.openglobes.core.trader.Response;
import com.openglobes.core.utils.QuickCondition;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InterceptorTest {

    private IInterceptorChain chain = null;
    private Integer requestCount = 0;
    private Integer respondCount = 0;

    private final QuickCondition cond = new QuickCondition();

    public InterceptorTest() {
    }

    @BeforeEach
    public void setUp() {
        chain = new InterceptorChain();
        assertDoesNotThrow(() -> {
            chain.addInterceptor(0,
                                 RequestInterceptingContext.class,
                                 Response.class,
                                 new IInterceptor<RequestInterceptingContext, Response>() {
                             @Override
                             public InterceptOperation onRequest(RequestInterceptingContext request,
                                                                 IInterceptorChain stack) {
                                 ++requestCount;
                                 return InterceptOperation.CONTINUE;
                             }

                             @Override
                             public InterceptOperation onResponse(Response response,
                                                                  IInterceptorChain stack) {
                                 fail("Intercepting should be terminated or skipped.");
                                 return InterceptOperation.CONTINUE;
                             }

                         });
            chain.addInterceptor(1,
                                 Response.class,
                                 new AbstractResponseInterceptor<Response>() {
                             @Override
                             public InterceptOperation onResponse(Response response,
                                                                  IInterceptorChain stack) {
                                 ++respondCount;
                                 cond.signalOne();
                                 return InterceptOperation.SKIP_REST;
                             }

                         });
            chain.addInterceptor(2,
                                 RequestInterceptingContext.class,
                                 new AbstractRequestInterceptor<RequestInterceptingContext>() {
                             @Override
                             public InterceptOperation onRequest(RequestInterceptingContext request,
                                                                 IInterceptorChain stack) {
                                 ++requestCount;
                                 cond.signalOne();
                                 return InterceptOperation.TERMINATE;
                             }
                         });
            chain.addInterceptor(3,
                                 RequestInterceptingContext.class,
                                 Response.class,
                                 new IInterceptor<RequestInterceptingContext, Response>() {
                             @Override
                             public InterceptOperation onRequest(RequestInterceptingContext request,
                                                                 IInterceptorChain stack) {
                                 fail("Intercepting should be terminated or skipped.");
                                 return InterceptOperation.CONTINUE;
                             }

                             @Override
                             public InterceptOperation onResponse(Response response,
                                                                  IInterceptorChain stack) {
                                 ++respondCount;
                                 return InterceptOperation.CONTINUE;
                             }

                         });
        });
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    @Order(0)
    public void testRequest() {
        var r = new RequestInterceptingContext(new Request(),
                                           new Instrument(),
                                           new Properties(),
                                           1);
        assertDoesNotThrow(() -> {
            chain.request(RequestInterceptingContext.class,
                          r);
            while (cond.waitSignal(1, TimeUnit.SECONDS)) {
                assertEquals(2,
                             requestCount);
                break;
            }
        });
    }

    @Test
    @Order(1)
    public void testRespond() {
        var r = new Response();
        assertDoesNotThrow(() -> {
            chain.respond(Response.class,
                          r);
            while (cond.waitSignal(1, TimeUnit.SECONDS)) {
                assertEquals(2,
                             respondCount);
                break;
            }
        });
    }
}
