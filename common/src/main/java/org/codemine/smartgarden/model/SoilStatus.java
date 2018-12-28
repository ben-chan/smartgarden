/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.model;

import java.util.Date;
import org.codemine.iot.device.sensor.ModbusSoilHumiditySensor;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author root
 */
@Entity
public class SoilStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTime;
    @Embedded
    private ModbusSoilHumiditySensor.OutputValue dryLevelAndTemperature;

    public SoilStatus(Long id, Date datetime, ModbusSoilHumiditySensor.OutputValue dryLevelAndTemperature) {
        this.id = id;
        this.dateTime = datetime;
        this.dryLevelAndTemperature = dryLevelAndTemperature;
    }

    public SoilStatus() {

    }

    /**
     * @return the datetime
     */
    public Date getDatetime() {
        return getDateTime();
    }

    /**
     * @param datetime the datetime to set
     */
    public void setDatetime(Date datetime) {
        this.setDateTime(datetime);
    }

    /**
     * @return the dryLevelAndTemperature
     */
    public ModbusSoilHumiditySensor.OutputValue getDryLevelAndTemperature() {
        return dryLevelAndTemperature;
    }

    /**
     * @param dryLevelAndTemperature the dryLevelAndTemperature to set
     */
    public void setDryLevelAndTemperature(ModbusSoilHumiditySensor.OutputValue dryLevelAndTemperature) {
        this.dryLevelAndTemperature = dryLevelAndTemperature;
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

}
