/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.market;

import com.openglobes.core.event.IEventSource;

/**
 *
 * @author Hongbao Chen
 * @since 1.0
 */
public interface INoticeSource {
    IEventSource getDataSource() throws NoticeSourceException;
}
