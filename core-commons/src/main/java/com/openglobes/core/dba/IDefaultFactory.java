/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.dba;

/**
 * @param <T>
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IDefaultFactory<T> {

    T contruct();
}
