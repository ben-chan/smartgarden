/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author root
 */
public class WaterFlow {
    private int id;
    private Date datime;
    private BigDecimal milliliter;

    public WaterFlow(BigDecimal milliliter) {
        this.milliliter = milliliter;
    }
    
    public WaterFlow(){
        
    }

    /**
     * @param milliliter the milliliter to set
     */
    public void setMilliliter(BigDecimal milliliter) {
        this.milliliter = milliliter;
    }

    /**
     * @return the milliliter
     */
    public BigDecimal getMilliliter() {
        return milliliter;
    }

    /**
     * @return the datime
     */
    public Date getDatime() {
        return datime;
    }

    /**
     * @param datime the datime to set
     */
    public void setDatime(Date datime) {
        this.datime = datime;
    }

}
