/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.market;

import com.openglobes.core.data.IMarketDataSource;
import com.openglobes.core.event.IEventSource;
import com.openglobes.core.exceptions.EngineException;
import java.util.Properties;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IMarketEngine {

    IEventSource getEventSource() throws EngineException;

    INoticeSource getNoticeSource() throws EngineException;

    void setDataSource(IMarketDataSource dataSource) throws EngineException;

    void registerMarket(int marketId, IMarketGateway gateway) throws EngineException;

    void unregisterMarket(int marketId) throws EngineException;

    void start(Properties properties) throws EngineException;

    void stop() throws EngineException;
}
