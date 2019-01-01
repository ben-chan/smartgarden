/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codemine.smartgarden.dto.RequestResult;
import org.codemine.smartgarden.model.IrrigationHistory;
import org.codemine.smartgarden.service.SmartGardenService;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;
import org.codemine.smartgarden.model.GardenStatus;
import org.codemine.smartgarden.model.PowerStatus;
import org.codemine.smartgarden.model.SoilStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author root
 */
@WebServlet(name = "SmartGardenServlet", urlPatterns = {"/web"}, asyncSupported = true)
public class SmartGardenServlet extends HttpServlet {

    public static String SERVICE_KEY = "SmartGardenService";
    private Logger logger = Logger.getLogger(SmartGardenServlet.class);

    @Autowired
    private SmartGardenService smartGardenService;

    @Override
    public void init() throws ServletException {
        super.init(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
            smartGardenService = (SmartGardenService) appContext.getBean("smartGardenService");
        } catch (Throwable t) {
            logger.log(Level.FATAL, "init", t);
        }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        DateFormat chartDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(chartDateFormat);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        RequestResult requestResult = new RequestResult();
        String action = (String) request.getParameter("action");
        if (action.equalsIgnoreCase("get_image")) {
            String filename = (String) request.getParameter("filename");
            File imageFile = smartGardenService.getImage(filename);
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            try (OutputStream outputStream = response.getOutputStream()) {
                ImageIO.write(bufferedImage, "jpg", outputStream);
            }
            return;
        }
        try {

            if (!StringUtils.isEmpty(action)) {
                if (action.equalsIgnoreCase("start_irrigation")) {
                    logger.log(Level.INFO, "processRequest:start_irrigation");
                    requestResult = new RequestResult<Integer>();
                    Integer historyId = smartGardenService.startIrrigationAsync();
                    requestResult.setSuccess(true);
                    requestResult.setValue(historyId);
                }
                if (action.equalsIgnoreCase("get_irrigation_history")) {
                    logger.log(Level.INFO, "processRequest:get_irrigation_history");
                    List<IrrigationHistory> historyList = smartGardenService.getIrrigationHistoryList(10);
                    requestResult.setValue(historyList);
                    requestResult.setSuccess(true);
                }

                if (action.equalsIgnoreCase("get_garden_status")) {
                    logger.log(Level.INFO, "processRequest:get_garden_status");
                    GardenStatus gardenStatus = smartGardenService.getGardenStatus();
                    requestResult.setValue(gardenStatus);
                    requestResult.setSuccess(true);
                }

                if (action.equalsIgnoreCase("stop_irrigation")) {
                    logger.log(Level.INFO, "processRequest:stop_irrigation");

                    String historyIdParam = request.getParameter("historyId");
                    Integer historyId = null;
                    if (!StringUtils.isEmpty(historyIdParam)) {
                        historyId = Integer.parseInt(historyIdParam);
                    }
                    smartGardenService.stopIrrigation(historyId);
                    requestResult.setSuccess(true);
                }

                if (action.equalsIgnoreCase("set_irrigation_duration")) {
                    logger.log(Level.INFO, "processRequest:set_irrigation_duration");
                    String irrigationDurationInSecond = (String) request.getParameter("duration");
                    final long newIrrigationDurationInSecond = smartGardenService.setIrrigationDuration(Integer.parseInt(irrigationDurationInSecond));
                    requestResult.setSuccess(true);
                    requestResult.setValue(newIrrigationDurationInSecond);
                }

                if (action.equalsIgnoreCase("get_soil_status_history")) {
                    logger.log(Level.INFO, "processRequest:get_soil_status_history");
                    List<SoilStatus> historyList = smartGardenService.getSoilStatusHistory(10);
                    requestResult.setValue(historyList);
                    requestResult.setSuccess(true);
                }

                if (action.equalsIgnoreCase("get_power_status_history")) {
                    logger.log(Level.INFO, "processRequest:get_power_status_history");
                    List<PowerStatus> historyList = smartGardenService.getPowerStatusHistory(10);
                    requestResult.setValue(historyList);
                    requestResult.setSuccess(true);
                }

                if (action.equalsIgnoreCase("take_photo")) {
                    logger.log(Level.INFO, "processRequest:take_photo");
                    String photoFilename = smartGardenService.takePhoto();
                    requestResult.setValue(photoFilename);
                    requestResult.setSuccess(true);
                }

            } else {
                request.getRequestDispatcher("/index.html").forward(request, response);
            }
        } catch (Throwable t) {
            logger.log(Level.ERROR, "processRequest", t);
            requestResult.setSuccess(false);
            requestResult.setErrorMessage(t.toString());
        } finally {
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            String responseJSON = mapper.writeValueAsString(requestResult);
            out.print(responseJSON);
            out.flush();
            out.close();
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
