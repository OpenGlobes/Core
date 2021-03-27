package com.openglobes.core.trader;

import com.openglobes.core.GatewayException;

/**
 * Underlying service that provides access to trading facilities.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface ITraderGateway {

    void setHandler(ITraderGatewayHandler handler) throws GatewayException;

    void insert(Request request) throws GatewayException;

    int getStatus();

    TraderGatewayInfo getGatewayInfo();
}
