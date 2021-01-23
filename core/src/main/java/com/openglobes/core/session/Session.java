/*
 * Copyright (C) 2020 Hongbao Chen <chenhongbao@outlook.com>
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
package com.openglobes.core.session;

import com.openglobes.core.IRequestContext;
import com.openglobes.core.IResponseContext;
import com.openglobes.core.RequestException;
import com.openglobes.core.ResponseException;
import com.openglobes.core.connector.ConnectorException;
import com.openglobes.core.data.DataQueryException;
import com.openglobes.core.interceptor.InterceptorException;
import com.openglobes.core.interceptor.RequestInterceptingContext;
import com.openglobes.core.trader.*;
import com.openglobes.core.utils.Utils;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
class Session implements ISession {

    private final IdMapper         map;
    private final IRequestContext  req;
    private final IResponseContext rsp;
    private       boolean          disposed;

    Session(IRequestContext request, IResponseContext response) {
        req      = request;
        rsp      = response;
        map      = new IdMapper();
        disposed = true;
    }

    @Override
    public void dispose() throws SessionException {
        check();
        disposed = false;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public <T> void request(Class<T> clazz, T object, Properties properties) throws AcquireInformationException,
                                                                                    ForwardRequestException {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(object);
        doRequest(clazz, object, properties);
    }

    @Override
    public <T> void respond(T object) throws ForwardResponseException,
                                             InvalidSessionResponseException,
                                             UnsupportedSessionResponseException {
        try {
            Objects.requireNonNull(object);
            adjustSrcId(object);
            if (object instanceof Trade) {
                rsp.getConnector().write((Trade) object);
            } else if (object instanceof Response) {
                rsp.getConnector().write((Response) object);
            } else if (object instanceof EngineRequestError) {
                rsp.getConnector().write((EngineRequestError) object);
            } else {
                throw new InvalidSessionResponseException(object.getClass().getCanonicalName());
            }
        } catch (ConnectorException | ResponseException ex) {
            throw new ForwardResponseException(ex.getMessage(),
                                               ex);
        } catch (NullPointerException ex) {
            throw new InvalidSessionResponseException("Response null ptr.",
                                                      ex);
        }
    }

    private void adjuestDestId(Request r) throws RequestException {
        if (r.getAction() == ActionType.NEW) {
            /*
             * Foe NEW action, register for a new destinated Id and set into
             * mapper.
             */
            var scrId = req.getSessionCorrelator().registerRequest(r, this);
            map.correlate(scrId, r.getOrderId());
        } else {
            /*
             * For DELETE action, recover destinated Id from mapper.
             */
            var destId = map.getDestIdBySrc(r.getOrderId());
            r.setOrderId(destId);
        }
    }

    private <T> void adjustSrcId(T object) throws UnsupportedSessionResponseException {
        if (object instanceof Trade) {
            var r = (Trade) object;
            r.setOrderId(map.getSrcIdByDest(r.getOrderId()));
        } else if (object instanceof Response) {
            var r = (Response) object;
            r.setOrderId(map.getSrcIdByDest(r.getOrderId()));
            checkRemoveMapping(r);
        } else if (object instanceof EngineRequestError) {
            var r = ((EngineRequestError) object).getRequest();
            Objects.requireNonNull(r);
            r.setOrderId(map.getSrcIdByDest(r.getOrderId()));
        } else {
            throw new UnsupportedSessionResponseException(object.getClass().getCanonicalName());
        }
    }

    private void check() throws SessionException {
        if (isDisposed()) {
            throw new SessionException("Sesion has been disposed.");
        }
    }

    private void checkRemoveMapping(Response response) {
        switch (response.getStatus()) {
            case OrderStatus.ALL_TRADED:
            case OrderStatus.DELETED:
            case OrderStatus.REJECTED:
                map.eraseByDestId(response.getOrderId());
                break;
            default:
                break;
        }
    }

    private <T> void doRequest(Class<T> clazz, T request, Properties properties) throws AcquireInformationException,
                                                                                        ForwardRequestException {
        if (clazz == Request.class) {
            doRequest((Request) request, properties);
        }
    }

    private void doRequest(Request request, Properties properties) throws AcquireInformationException,
                                                                          ForwardRequestException {
        try {
            adjuestDestId(request);
            req.getSharedContext().getInterceptorStack()
               .request(RequestInterceptingContext.class,
                        new RequestInterceptingContext(request,
                                                       getInstrument(request.getInstrumentId()),
                                                       properties,
                                                       Utils.nextId().intValue()));
        } catch (RequestException | InterceptorException ex) {
            throw new ForwardRequestException(ex.getMessage(),
                                              ex);
        }
    }

    private Instrument getInstrument(String instrumentId) throws RequestException,
                                                                 AcquireInformationException {
        var ds = req.getTraderEngine().getDataSource();
        try (var conn = ds.getConnection()) {
            return conn.getInstrumentById(instrumentId);
        } catch (DataQueryException | SQLException | ClassNotFoundException ex) {
            throw new AcquireInformationException(ex.getMessage(),
                                                  ex);
        }
    }

    private class IdMapper {

        private final Map<Long, Long> toDest;
        private final Map<Long, Long> toSrc;

        IdMapper() {
            toDest = new ConcurrentHashMap<>(1024);
            toSrc  = new ConcurrentHashMap<>(1024);
        }

        public void correlate(Long srcId, Long destId) {
            toDest.put(srcId, destId);
            toSrc.put(destId, srcId);
        }

        public void eraseByDestId(Long destId) {
            var srcId = getSrcIdByDest(destId);
            toDest.remove(srcId);
            toSrc.remove(destId);
        }

        public Long getDestIdBySrc(Long srcId) {
            Objects.requireNonNull(srcId);
            var r = toDest.get(srcId);
            Objects.requireNonNull(r);
            return r;
        }

        public Long getSrcIdByDest(Long destId) {
            Objects.requireNonNull(destId);
            var r = toSrc.get(destId);
            Objects.requireNonNull(r);
            return r;
        }

    }
}
