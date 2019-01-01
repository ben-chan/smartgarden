/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden;

import org.codemine.smartgarden.service.SmartGardenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author BENCPCHAN
 */
@SpringBootApplication
public class ConsoleApplication implements CommandLineRunner {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ConsoleApplication.class);

    @Autowired
    private SmartGardenService smartGardenService;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Throwable {
        SpringApplication application = new SpringApplication(ConsoleApplication.class);
        for (String arg : args) {
            if (arg.contains("--spring.profiles.active=dev")) {
                application.setAdditionalProfiles("dev");
            }
        }
        application.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            String action = args[0];
            if ("healthcheck".equals(action)) {
                smartGardenService.healthCheck();
            }
            if ("startIrrigationWhenLowHumidity".equals(action)) {
                smartGardenService.startIrrigationWhenLowHumidity();
            }
            smartGardenService.shutdown();
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }
}
