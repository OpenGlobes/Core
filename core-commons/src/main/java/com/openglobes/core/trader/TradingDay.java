/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openglobes.core.trader;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * @author Hongbao Chen
 * @since 1.0
 */
public class TradingDay implements Serializable {

    private LocalDate     actionDay;
    private ZonedDateTime timestamp;
    private LocalDate     tradingDay;
    private Long          tradingDayId;

    public TradingDay() {
    }

    public LocalDate getActionDay() {
        return actionDay;
    }

    public void setActionDay(LocalDate actionDay) {
        this.actionDay = actionDay;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDate getTradingDay() {
        return tradingDay;
    }

    public void setTradingDay(LocalDate tradingDay) {
        this.tradingDay = tradingDay;
    }

    public Long getTradingDayId() {
        return tradingDayId;
    }

    public void setTradingDayId(Long tradingDayId) {
        this.tradingDayId = tradingDayId;
    }

}
