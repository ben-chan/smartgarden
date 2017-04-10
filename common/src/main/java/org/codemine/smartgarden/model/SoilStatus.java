/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.model;

import java.util.Date;
import com.codemine.iot.device.sensor.ModbusSoilHumiditySensor;

/**
 *
 * @author root
 */
public class SoilStatus {    

    private long id;
    private Date datetime;
    private ModbusSoilHumiditySensor.Data dryLevelAndTemperature;
    
    public SoilStatus(long id, Date datetime, ModbusSoilHumiditySensor.Data dryLevelAndTemperature) {
        this.id = id;
        this.datetime = datetime;
        this.dryLevelAndTemperature = dryLevelAndTemperature;
    }    

    public SoilStatus() {
        
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the datetime
     */
    public Date getDatetime() {
        return datetime;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @param datetime the datetime to set
     */
    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    /**
     * @return the dryLevelAndTemperature
     */
    public ModbusSoilHumiditySensor.Data getDryLevelAndTemperature() {
        return dryLevelAndTemperature;
    }

    /**
     * @param dryLevelAndTemperature the dryLevelAndTemperature to set
     */
    public void setDryLevelAndTemperature(ModbusSoilHumiditySensor.Data dryLevelAndTemperature) {
        this.dryLevelAndTemperature = dryLevelAndTemperature;
    }
   
   
}
