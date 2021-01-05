/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.market;

import com.openglobes.core.exceptions.GatewayException;
import java.util.Properties;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IMarketGateway {

    void start(Properties properties, IMarketGatewayHandler handler) throws GatewayException;

    void stop() throws GatewayException;

    int getStatus();

    Properties getProperties();

    MarketGatewayInfo getGatewayInfo();
}
