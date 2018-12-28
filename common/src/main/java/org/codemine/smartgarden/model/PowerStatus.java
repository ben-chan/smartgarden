/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.model;

import java.util.Date;
import org.codemine.iot.device.sensor.INA219VoltageCurrentSensor;
import java.io.Serializable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author demof
 */
@Entity
public class PowerStatus implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTime;
    @Embedded
    private INA219VoltageCurrentSensor.OutputValue voltageAndCurrent;
    private int batteryLevelInPercent;

    public PowerStatus(Long id, Date dateTime, INA219VoltageCurrentSensor.OutputValue voltageAndCurrent) {
        this.id = id;
        this.dateTime = dateTime;
        this.voltageAndCurrent = voltageAndCurrent;
    }

    public PowerStatus() {

    }

    /**
     * @return the voltageAndCurrent
     */
    public INA219VoltageCurrentSensor.OutputValue getVoltageAndCurrent() {
        return voltageAndCurrent;
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
     * @return the dateTime
     */
    public Date getDateTime() {
        return dateTime;
    }

    /**
     * @param dateTime the dateTime to set
     */
    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

}
