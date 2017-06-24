/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codemine.smartgarden;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.log4j.Priority;
import org.codemine.smartgarden.service.SmartGardenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 *
 * @author BENCPCHAN
 */
@SpringBootApplication
public class ConsoleApplication {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ConsoleApplication.class);

    @Value("${jdbc.url}")
    private String jdbcUrl;
    @Value("${jdbc.username}")
    private String jdbcUsername;
    @Value("${jdbc.password}")
    private String jdbcPassword;

    public void start(String action) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        try {
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUrl(jdbcUrl);
            dataSource.setUsername(jdbcUsername);
            dataSource.setPassword(jdbcPassword);            
            SmartGardenService smartGardenService =  new SmartGardenService(dataSource);
            if ("healthcheck".equals(action)) {
                smartGardenService.healthCheck(dataSource);
            }
            if ("startIrrigationWhenLowHumidity".equals(action)) {
                smartGardenService.startIrrigationWhenLowHumidity(dataSource);
            }
            smartGardenService.shutdown();
        } catch (Throwable t) {
            logger.log(Priority.ERROR, "Console:main", t);
        } finally {
            try {
                if (dataSource.getConnection() != null) {
                    dataSource.getConnection().close();
                }
            } catch (Throwable t) {
            }
        }
    }
    
     

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder()
                .sources(ConsoleApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);

        ConsoleApplication app = context.getBean(ConsoleApplication.class);
        app.start(args[0]);

    }
}
