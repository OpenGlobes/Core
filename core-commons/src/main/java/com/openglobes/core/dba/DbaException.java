/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.dba;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public class DbaException extends Exception {

    private static final long serialVersionUID = 12427834234L;

    public DbaException(String message) {
        super(message);
    }

    public DbaException(String message, Throwable cause) {
        super(message, cause);
    }

}
