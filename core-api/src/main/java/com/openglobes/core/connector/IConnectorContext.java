/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.connector;

import com.openglobes.core.IConfiguredContext;
import com.openglobes.core.session.ISession;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public interface IConnectorContext extends IConfiguredContext<IConnector> {

    ISession getSession();
}
