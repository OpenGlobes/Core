/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.data;

import com.openglobes.core.exceptions.ServiceStatus;

/**
 *
 * @author chenh
 */
public class MarketDataSourceException extends ServiceStatus {

    private static final long serialVersionUID = 3256398000718765L;
    
    public MarketDataSourceException(Integer code, String msg) {
        super(code, msg);
    }

    public MarketDataSourceException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }
    
}
