/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.exceptions;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public enum Exceptions {
    DATASOURCE_CLOSE_FAIL(0x2000, "Data source close failed.");
    
    private final int code;
    private final String message;

    private Exceptions(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
