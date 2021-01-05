/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.market;

import com.openglobes.core.exceptions.ServiceStatus;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class NoticeSourceException extends ServiceStatus{

    private static final long serialVersionUID = 326544872983L;
    
    public NoticeSourceException(Integer code, String msg) {
        super(code, msg);
    }

    public NoticeSourceException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }
    
}
