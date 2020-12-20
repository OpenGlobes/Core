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
public enum Offset {
    OPEN(0x90),
    CLOSE(0x91),
    CLOSE_TODAY(0x92);

    private final int code;

    private Offset(int code) {
        this.code = code;
    }

    public int code9() {
        return code;
    }
}
