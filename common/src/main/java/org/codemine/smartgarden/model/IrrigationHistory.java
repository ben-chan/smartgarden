/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author root
 */
public class IrrigationHistory implements Serializable{
    private Integer id;
    private Date startTime;
    private Date endTime;
    private String imageFilename;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

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
}
