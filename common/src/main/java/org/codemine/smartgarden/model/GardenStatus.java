/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author demof
 */
public class GardenStatus {

    private PowerStatus powerStatus = new PowerStatus();
    private IrrigationStatus irrigationStatus = new IrrigationStatus();

    /**
     * @return the powerStatus
     */
    public PowerStatus getPowerStatus() {
        return powerStatus;
    }

    /**
     * @param powerStatus the powerStatus to set
     */
    public void setPowerStatus(PowerStatus powerStatus) {
        this.powerStatus = powerStatus;
    }

    /**
     * @return the irrigationStatus
     */
    public IrrigationStatus getIrrigationStatus() {
        return irrigationStatus;
    }

    /**
     * @param irrigationStatus the irrigationStatus to set
     */
    public void setIrrigationStatus(IrrigationStatus irrigationStatus) {
        this.irrigationStatus = irrigationStatus;
    }

}
