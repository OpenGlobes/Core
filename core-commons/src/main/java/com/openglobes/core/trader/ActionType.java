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
public class ActionType implements Serializable {

    public static final int DELETE = (0x81);
    public static final int NEW    = (0x80);

    private ActionType() {
    }
}
