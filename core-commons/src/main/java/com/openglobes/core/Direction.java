/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public enum Direction {

    BUY(0xA0),
    SELL(0xA1);

    private final int code;

    private Direction(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
