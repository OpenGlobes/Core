/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.data;

import java.util.Properties;

/**
 *
 * @author chenh
 */
public class DataSourceData {

    private final AbstractTraderDataSource ds = new DefaultTraderDataSource();
    private final Properties props = new Properties();

    protected DataSourceData() {
        setProperties();
        setDataSource();
    }

    protected ITraderDataSource dataSource() {
        return ds;
    }

    private void setDataSource() {
        ds.open(props);
    }

    private void setProperties() {
        /*
         * Set properties.
         */
        props.put("DataSource.URL", "jdbc:h2:mem:default-db");
        props.put("DataSource.DriverClass", "org.h2.Driver");
        props.put("USER", "sa");
        props.put("PASSWORD", "");
    }
}
