package com.openglobes.core.trader;

/**
 * Underlying service that provides access to trading facilities.
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface ITraderGateway {

    void setHandler(ITraderGatewayHandler handler);

    void insert(Request request);

    int getStatus();

    TraderGatewayInfo getGatewayInfo();
}
