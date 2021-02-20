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
public class Offset implements Serializable {

    public static final int OPEN = (0x90);
    public static final int CLOSE_YD = (0x91);
    public static final int CLOSE_TODAY = (0x92);
    public static final int CLOSE_AUTO = (0x93);

    private Offset() {
    }
}
