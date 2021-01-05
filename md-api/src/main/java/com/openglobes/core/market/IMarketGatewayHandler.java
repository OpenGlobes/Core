/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.market;

import com.openglobes.core.market.Tick;
import com.openglobes.core.exceptions.GatewayRuntimeException;
import com.openglobes.core.exceptions.ServiceRuntimeStatus;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IMarketGatewayHandler {

    void onTick(Tick tick);

    void onException(GatewayRuntimeException exception);

    void onStatusChange(ServiceRuntimeStatus status);
}
