/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
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
public class IrrigationHistory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
    private String imageFilename;
    @Column(name="water_volume_in_ml")
    private int waterVolumeInML;

    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the imageFilename
     */
    public String getImageFilename() {
        return imageFilename;
    }

    /**
     * @param imageFilename the imageFilename to set
     */
    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    /**
     * @return the waterVolumeInML
     */
    public int getWaterVolumeInML() {
        return waterVolumeInML;
    }

    /**
     * @param waterVolumeInML the waterVolumeInML to set
     */
    public void setWaterVolumeInML(int waterVolumeInML) {
        this.waterVolumeInML = waterVolumeInML;
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
