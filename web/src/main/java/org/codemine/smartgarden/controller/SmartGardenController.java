/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.codemine.smartgarden.dto.RequestResult;
import org.codemine.smartgarden.model.GardenStatus;
import org.codemine.smartgarden.model.IrrigationHistory;
import org.codemine.smartgarden.model.PowerStatus;
import org.codemine.smartgarden.model.SoilStatus;
import org.codemine.smartgarden.service.SmartGardenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author demof
 */
@RestController
@RequestMapping("/smartgarden2")
public class SmartGardenController {

    @Autowired
    private SmartGardenService smartGardenService;

    @RequestMapping("/takePhoto")
    public RequestResult takePhoto() throws ParseException {
        String filename = smartGardenService.takePhoto();
        RequestResult requestResult = new RequestResult();
        requestResult.setSuccess(true);
        requestResult.setErrorMessage(filename);
        return requestResult;
    }

    @RequestMapping(value = "/getImage", method = RequestMethod.GET)
    public void getImage(@RequestParam(name = "filename", required = true) String filename, HttpServletResponse response) throws IOException {
        smartGardenService.getImage(filename);
        File imageFile = smartGardenService.getImage(filename);
        BufferedImage bufferedImage = ImageIO.read(imageFile);
        try (OutputStream outputStream = response.getOutputStream()) {
            ImageIO.write(bufferedImage, "jpg", outputStream);
        }
    }

    @RequestMapping("/startIrrigation")
    public RequestResult startIrrigation() {
        RequestResult requestResult = new RequestResult<Integer>();
        Integer historyId = null;
        try {
            historyId = smartGardenService.startIrrigationAsync();
            requestResult.setSuccess(true);
        } catch (SQLException ex) {
            requestResult.setSuccess(false);
        }

        requestResult.setValue(historyId);
        return requestResult;
    }

    @RequestMapping("/getIrrigationHistory")
    public RequestResult getIrrigationHistory() {
        RequestResult requestResult = new RequestResult<>();
        List<IrrigationHistory> historyList = smartGardenService.getIrrigationHistoryList(10);
        requestResult.setValue(historyList);
        requestResult.setSuccess(true);
        return requestResult;
    }

    @RequestMapping("/getGardenStatus")
    public RequestResult getGardenStatus() throws Throwable {
        RequestResult requestResult = new RequestResult<>();
        GardenStatus gardenStatus = smartGardenService.getGardenStatus();
        requestResult.setValue(gardenStatus);
        requestResult.setSuccess(true);
        return requestResult;
    }

    @RequestMapping("/stopIrrigation")
    public RequestResult stopIrrigation(@RequestParam(name = "historyId", required = true) String historyIdParam) {
        RequestResult requestResult = new RequestResult<>();
        Integer historyId = null;
        if (!StringUtils.isEmpty(historyIdParam)) {
            historyId = Integer.parseInt(historyIdParam);
        }
        smartGardenService.stopIrrigation(historyId);
        requestResult.setSuccess(true);
        return requestResult;
    }

    @RequestMapping("/setIrrigationDuration")
    public RequestResult setIrrigationDuration(@RequestParam(name = "duration", required = true) String irrigationDurationInSecond) {
        RequestResult requestResult = new RequestResult<>();
        final long newIrrigationDurationInSecond = smartGardenService.setIrrigationDuration(Integer.parseInt(irrigationDurationInSecond));
        requestResult.setSuccess(true);
        requestResult.setValue(newIrrigationDurationInSecond);
        requestResult.setSuccess(true);
        return requestResult;
    }

    @RequestMapping("/getSoilStatusHistory")
    public RequestResult getSoilStatusHistory() {
        RequestResult requestResult = new RequestResult<>();
        List<SoilStatus> historyList = smartGardenService.getSoilStatusHistory(10);
        requestResult.setValue(historyList);
        requestResult.setSuccess(true);
        requestResult.setSuccess(true);

        requestResult.setSuccess(true);
        return requestResult;
    }

    @RequestMapping("/getPowerStatusHistory")
    public RequestResult getPowerStatusHistory() {
        RequestResult requestResult = new RequestResult<>();
        List<PowerStatus> historyList = smartGardenService.getPowerStatusHistory(10);
        requestResult.setValue(historyList);
        requestResult.setSuccess(true);
        return requestResult;
    }

}
