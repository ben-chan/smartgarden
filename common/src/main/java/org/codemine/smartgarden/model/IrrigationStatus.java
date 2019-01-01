/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.model;

/**
 *
 * @author root
 */
public class IrrigationStatus {

    private boolean waterValveOpen = false;
    private WaterflowStatus waterflow = null;
    private SoilStatus soilStatus = null;

    public IrrigationStatus(WaterflowStatus waterflow, SoilStatus soilStatus) {
        this.waterflow = waterflow;
        this.soilStatus = soilStatus;
    }

    public IrrigationStatus() {

    }

    /**
     * @return the waterflow
     */
    public WaterflowStatus getWaterflow() {
        return waterflow;
    }

    /**
     * @param waterflow the waterflow to set
     */
    public void setWaterflow(WaterflowStatus waterflow) {
        this.waterflow = waterflow;
    }

    /**
     * @return the soilStatus
     */
    public SoilStatus getSoilStatus() {
        return soilStatus;
    }

    /**
     * @param soilStatus the soilStatus to set
     */
    public void setSoilStatus(SoilStatus soilStatus) {
        this.soilStatus = soilStatus;
    }

    /**
     * @return the waterValveOpen
     */
    public boolean isWaterValveOpen() {
        return waterValveOpen;
    }

    /**
     * @param waterValveOpen the waterValveOpen to set
     */
    public void setWaterValveOpen(boolean waterValveOpen) {
        this.waterValveOpen = waterValveOpen;
    }

}
