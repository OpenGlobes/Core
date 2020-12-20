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
public enum ActionType {
    NEW(0x80),
    DELETE(0x81);

    private final int code;

    private ActionType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
