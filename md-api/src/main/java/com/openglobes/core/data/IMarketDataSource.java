/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.data;

import java.util.Properties;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IMarketDataSource {
    IMarketData getConnection() throws MarketDataSourceException;
    
     void open(Properties properties) throws MarketDataSourceException;
}
