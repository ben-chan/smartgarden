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
    private Date dateTIme;
    private INA219VoltageCurrentSensor.OutputValue voltageAndCurrent;
    private int batteryLevelInPercent;

    public PowerStatus(int id, Date dateTIme, INA219VoltageCurrentSensor.OutputValue voltageAndCurrent) {
        this.id = id;
        this.dateTIme = dateTIme;
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
     * @return the dateTIme
     */
    public Date getDateTIme() {
        return dateTIme;
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
     * @param dateTIme the dateTIme to set
     */
    public void setDateTIme(Date dateTIme) {
        this.dateTIme = dateTIme;
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

   
}
