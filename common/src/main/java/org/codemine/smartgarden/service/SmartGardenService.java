/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.service;

import com.mysql.cj.api.jdbc.Statement;
import com.pi4j.io.gpio.GpioController;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;
import javax.annotation.PostConstruct;

import javax.imageio.ImageIO;
import org.codemine.iot.device.camera.Camera;
import org.codemine.iot.device.sensor.EventDrivenSensor;
import org.codemine.iot.device.sensor.HallEffectWaterFlowSensor;
import org.codemine.iot.device.sensor.INA219VoltageCurrentSensor;
import org.codemine.iot.device.sensor.ModbusSoilHumiditySensor;
import org.codemine.iot.device.sensor.Sensor;
import org.codemine.iot.device.valve.Valve;
import org.codemine.iot.device.valve.WaterValve;
import org.codemine.smartgarden.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author root
 */
@Service
public class SmartGardenService implements Closeable {

    private static final AtomicBoolean inProgress = new AtomicBoolean(false);
    private static final Logger logger = Logger.getLogger(SmartGardenService.class);
    private final AtomicLong irrigationDurationInSecond = new AtomicLong(90);

    private long soilMaxDryLevelAllowed = 2000;
    private long soilDryLevelPollCount = 30;
    private long soilDryLevelPollFrequencyInMS = 2000;

    private String mediaFileDirectory = "/tmp";

    @Autowired
    private Camera camera = null;

    @Autowired
    private Sensor<ModbusSoilHumiditySensor.OutputValue> soilHumiditySensor = null;

    @Autowired
    private EventDrivenSensor<HallEffectWaterFlowSensor.OutputValue> waterFlowSensor = null;

    @Autowired
    private Sensor<INA219VoltageCurrentSensor.OutputValue> voltageCurrentSensor = null;

    @Autowired
    private Valve waterValve = null;

    @Autowired
    private NotificationService notificationService = null;

    @Autowired
    private HealthCheckService healthCheckService = null;

    @Autowired
    private GpioController gpioController = null;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ConfigService configService;

    public SmartGardenService() {

    }

    @Bean
    private static NotificationService notificationService(ConfigService configService) {
        return new NotificationService(configService.getValue("notification.email"), configService.getValue("notification.email.password"));
    }

    @PostConstruct
    public void init() throws Exception {
        // provision gpio pin #01 as an output pin and turn on
        this.configService.initDefaultConfigIfMissing();
        this.irrigationDurationInSecond.set(configService.getValue("irrigation.durationInSeconds", Long::parseLong));
        this.soilMaxDryLevelAllowed = configService.getValue("soil.maxDryLevelAllowed", Long::parseLong);

        this.soilDryLevelPollCount = configService.getValue("soil.dryLevelPollCount", Long::parseLong);
        this.soilDryLevelPollFrequencyInMS = configService.getValue("soil.dryLevelPollFrequencyInMS", Long::parseLong);

        this.mediaFileDirectory = configService.getValue("monitor.mediaFilePath");

    }

    public File getImage(String filename) {
        return new File(configService.getValue("monitor.mediaFilePath"), filename);
    }

    public void shutdown() {
        this.gpioController.shutdown();
    }

    public void healthCheck() throws DataAccessException, Throwable {
        if (this.healthCheckService.isValveOpenAfterIrrigation(this.irrigationDurationInSecond.get())) {
            this.stopIrrigation(null);
        }
        healthCheckService.checkSolarPowerLevel();
        this.healthCheckService.checkWaterflow();
    }

    public long setIrrigationDuration(long durationInSecond) {
        irrigationDurationInSecond.set(durationInSecond);
        return irrigationDurationInSecond.get();
    }

    @Transactional(readOnly = false)
    public void startIrrigationWhenLowHumidity() {
        try {

            int dryLevelSum = 0;
            int temperatureSum = 0;
            for (int i = 0; i < soilDryLevelPollCount; ++i) {
                ModbusSoilHumiditySensor.OutputValue humidtyAndTemperature = this.soilHumiditySensor.readOutputValue();
                temperatureSum += humidtyAndTemperature.getTemperatureInCelsius();
                dryLevelSum += humidtyAndTemperature.getDryLevel();
                Thread.sleep(soilDryLevelPollFrequencyInMS);
                logger.info(String.format("dryLevel %s temperature %s", humidtyAndTemperature.getDryLevel(),
                        humidtyAndTemperature.getTemperatureInCelsius()
                ));
            }
            final long averageDryLevel = dryLevelSum / soilDryLevelPollCount;
            final long averageTemperature = temperatureSum / soilDryLevelPollCount;
            if (averageDryLevel > soilMaxDryLevelAllowed) {
                this.startIrrigationAsync();
            }
            String sql = "INSERT INTO soil_status (date_time,dry_level,temperature_in_celsius) values(now(),?,?)";
            jdbcTemplate.update(sql, new Object[]{averageDryLevel, averageTemperature});
        } catch (Throwable t) {
            logger.fatal("startIrrigationWhenLowHumdity", t);
        } finally {

        }

    }

    public Integer startIrrigationAsync() throws SQLException {
        try {
            if (this.inProgress.get()) {
                return -1;
            }
            this.inProgress.set(true);
            this.waterFlowSensor.startListenEvent();
            this.waterValve.on();
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement statement = connection.prepareStatement(
                            "insert into irrigation_history (start_time) values (now())", Statement.RETURN_GENERATED_KEYS);
                    return statement;
                }
            }, keyHolder);
            final int historyId = keyHolder.getKey().intValue();
            logger.log(Level.INFO, String.format("start setPinStatus High %s,historyId=%s", new Date().toString(), historyId));

            this.notificationService.sendAsyncEmail("Irrigation Started",
                    String.format("HistoryId=%s, time=%s", historyId, new Date().toString()));

            final SmartGardenService self = this;

            Thread irrigationThread = new Thread() {
                @Override
                public void run() {
                    try {
                        final long duration = irrigationDurationInSecond.get() * 1000;
                        Thread.sleep(duration);
                        self.stopIrrigation(historyId);
                        self.waterFlowSensor.stopListenEvent();
                        logger.log(Level.INFO, String.format("start setPinStatus Low datetime=%s, duration=%s, historyId=%s", new Date().toString(), duration, historyId));

                        notificationService.sendAsyncEmail("Irrigation Finished",
                                String.format("HistoryId=%s, time=%s waterflow=%s ml", historyId, new Date().toString(),
                                        self.waterFlowSensor.readOutputValue().getTotalMillilitre().toBigInteger()
                                ));

                    } catch (Throwable t) {
                        logger.log(Level.ERROR, "stopThread", t);
                    }
                }

            };
            irrigationThread.start();
            return historyId;
        } catch (Throwable t) {
            if (this.waterValve.getStatus() == WaterValve.Status.On) {
                this.waterValve.off();
            }
            inProgress.set(false);
            logger.log(Level.ERROR, "start", t);
        }
        return -1;
    }

    private double calculatePowerLevelInPercent(double voltage) {
        if (voltage < 10.5) {
            return 0.0;
        }
        if (voltage >= 10.5 && voltage < 11.5) {
            return 25.0;
        }
        if (voltage >= 11.5 && voltage < 12.5) {
            return 50;
        }
        if (voltage >= 12.5 && voltage < 13.5) {
            return 75;
        }
        if (voltage >= 13.5) {
            return 100;
        }
        return -1;
    }

    public String takePhoto() {
        String imageFilename = null;
        try {
            imageFilename = UUID.randomUUID().toString() + ".jpg";
            BufferedImage photoImage = this.camera.takePhoto();
            File imageFile = new File(mediaFileDirectory, imageFilename);
            ImageIO.write(photoImage, "jpg", imageFile);
            if (!imageFile.exists()) {
                throw new FileNotFoundException(imageFilename);
            }
        } catch (Throwable t) {
            logger.error("take photo", t);
        }
        return imageFilename;
    }

    public boolean stopIrrigation(Integer historyId) {
        if (!inProgress.get()) {
            return true;
        }
        this.waterValve.off();
        inProgress.set(false);
        try {
            String imageFilename = this.takePhoto();
            jdbcTemplate.update("update irrigation_history set end_time=now(),image_filename=?,water_volume_in_ml=? where id=? and end_time is null",
                    new Object[]{imageFilename, this.waterFlowSensor.readOutputValue().getTotalMillilitre(), historyId});
        } catch (Throwable t) {
            logger.error("update database", t);
        }

        return true;
    }

    public List<SoilStatus> getSoilStatusHistory(int itemCount) {
        return jdbcTemplate.query(String.format("select id,dry_level,temperature_in_celsius,date_time from soil_status order by id desc limit %s", itemCount),
                new RowMapper<SoilStatus>() {
            @Override
            public SoilStatus mapRow(ResultSet rs, int i) throws SQLException {
                SoilStatus soilStatus = new SoilStatus();
                soilStatus.setId(rs.getLong("id"));
                soilStatus.setDatetime(new Timestamp(rs.getTimestamp("date_time").getTime()));
                ModbusSoilHumiditySensor.OutputValue drylevelAndTemperature = new ModbusSoilHumiditySensor.OutputValue(rs.getInt("temperature_in_celsius"), rs.getInt("dry_level"));
                soilStatus.setDryLevelAndTemperature(drylevelAndTemperature);
                return soilStatus;
            }

        }, new Object[]{});
    }

    public List<PowerStatus> getPowerStatusHistory(int itemCount) {
        return jdbcTemplate.query(String.format("select id,voltage,current_in_ma,date_time from power_status order by id desc limit %s", itemCount),
                new RowMapper<PowerStatus>() {
            @Override
            public PowerStatus mapRow(ResultSet rs, int i) throws SQLException {
                PowerStatus powerStatus = new PowerStatus();
                powerStatus.setId(rs.getLong("id"));
                powerStatus.setDateTime(new Timestamp(rs.getTimestamp("date_time").getTime()));
                INA219VoltageCurrentSensor.OutputValue voltageAndCurrent
                        = new INA219VoltageCurrentSensor.OutputValue(rs.getDouble("voltage"), rs.getDouble("current_in_ma"));
                powerStatus.setVoltageAndCurrent(voltageAndCurrent);
                return powerStatus;
            }

        }, new Object[]{});
    }

    public IrrigationHistory getLastIrrigationHistory() {
        List<IrrigationHistory> irrigationHistoryList = getIrrigationHistoryList(1);
        if (irrigationHistoryList.isEmpty()) {
            return new IrrigationHistory();
        }
        return irrigationHistoryList.get(0);
    }

    public List<IrrigationHistory> getIrrigationHistoryList(int itemCount) {
        return jdbcTemplate.query(String.format("select id,start_time,end_time,image_filename,water_volume_in_ml from irrigation_history order by id desc limit %s", itemCount),
                new RowMapper<IrrigationHistory>() {
            @Override
            public IrrigationHistory mapRow(ResultSet rs, int i) throws SQLException {
                IrrigationHistory history = new IrrigationHistory();
                history.setId(rs.getLong("id"));
                history.setStartTime(new Date(rs.getTimestamp("start_time").getTime()));
                if (rs.getTimestamp("end_time") != null) {
                    history.setEndTime(new Date(rs.getTimestamp("end_time").getTime()));
                }
                history.setImageFilename(rs.getString("image_filename"));
                history.setWaterVolumeInML(rs.getInt("water_volume_in_ml"));
                return history;
            }

        }, new Object[]{});
    }

    public GardenStatus getGardenStatus() throws Throwable {
        GardenStatus gardenStatus = new GardenStatus();

        PowerStatus powerStatus = new PowerStatus();
        powerStatus.setVoltageAndCurrent(this.voltageCurrentSensor.readOutputValue());
        powerStatus.setBatteryLevelInPercent((int) calculatePowerLevelInPercent(powerStatus.getVoltageAndCurrent().getVoltage()));
        gardenStatus.setPowerStatus(powerStatus);

        gardenStatus.setIrrigationStatus(this.getIrrigationStatus());
        gardenStatus.setConfig(this.configService.getAllConfig());
        return gardenStatus;
    }

    private IrrigationStatus getIrrigationStatus() throws Throwable {

        IrrigationStatus irrigationStatus = new IrrigationStatus();
        SoilStatus soilStatus = new SoilStatus();
        soilStatus.setDryLevelAndTemperature(this.soilHumiditySensor.readOutputValue());
        irrigationStatus.setSoilStatus(soilStatus);

        irrigationStatus.setWaterValveOpen(this.waterValve.getStatus() == Valve.Status.On);

        WaterflowStatus waterFlow = new WaterflowStatus();
        waterFlow.setVolumeInML(this.waterFlowSensor.readOutputValue().getTotalMillilitre());
        irrigationStatus.setWaterflow(waterFlow);

        return irrigationStatus;
    }

    @Override
    public void close() throws IOException {
        this.gpioController.shutdown();
    }

}
