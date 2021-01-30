/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.trader;

import java.io.Serializable;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class Direction implements Serializable {

    public static final int BUY  = (0xA0);
    public static final int SELL = (0xA1);

    private Direction() {
    }
}
