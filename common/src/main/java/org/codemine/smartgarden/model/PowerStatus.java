/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.model;

import java.util.Date;
import com.codemine.iot.device.sensor.INA219VoltageCurrentSensor;

/**
 *
 * @author demof
 */
public class PowerStatus {
    private int id;
    private Date datetime;
    private INA219VoltageCurrentSensor.OutputValue voltageAndCurrent;
    private int batteryLevelInPercent;

    public PowerStatus(int id, Date datetime, INA219VoltageCurrentSensor.OutputValue voltageAndCurrent) {
        this.id = id;
        this.datetime = datetime;
        this.voltageAndCurrent = voltageAndCurrent;
    }

    public PowerStatus() {
        
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the voltageAndCurrent
     */
    public INA219VoltageCurrentSensor.OutputValue getVoltageAndCurrent() {
        return voltageAndCurrent;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * @param voltageAndCurrent the voltageAndCurrent to set
     */
    public void setVoltageAndCurrent(INA219VoltageCurrentSensor.OutputValue voltageAndCurrent) {
        this.voltageAndCurrent = voltageAndCurrent;
    }

    /**
     * @return the batteryLevelInPercent
     */
    public int getBatteryLevelInPercent() {
        return batteryLevelInPercent;
    }

    /**
     * @param batteryLevelInPercent the batteryLevelInPercent to set
     */
    public void setBatteryLevelInPercent(int batteryLevelInPercent) {
        this.batteryLevelInPercent = batteryLevelInPercent;
    }

    /**
     * @return the datetime
     */
    public Date getDatetime() {
        return datetime;
    }

    /**
     * @param datetime the datetime to set
     */
    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

   
}
