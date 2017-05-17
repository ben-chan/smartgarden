/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.service;

import com.mysql.cj.api.jdbc.Statement;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import java.awt.Dimension;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.codemine.iot.device.camera.Camera;
import com.codemine.iot.device.camera.MockCamera;
import com.codemine.iot.device.camera.USBCamera;
import com.codemine.iot.device.sensor.EventDrivenSensor;
import com.codemine.iot.device.sensor.MockEventDrivenSensor;
import com.codemine.iot.device.sensor.MockPollingSensor;
import com.codemine.iot.device.sensor.Sensor;
import com.codemine.iot.device.sensor.ModbusSoilHumiditySensor;
import com.codemine.iot.device.sensor.INA219VoltageCurrentSensor;
import com.codemine.iot.device.sensor.HallEffectWaterFlowSensor;
import com.codemine.iot.device.valve.MockValve;
import com.codemine.iot.device.valve.Valve;
import com.codemine.iot.device.valve.WaterValve;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.codemine.smartgarden.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 *
 * @author root
 */
public class SmartGardenService {

    private static final AtomicBoolean inProgress = new AtomicBoolean(false);
    private static final Logger logger = Logger.getLogger(SmartGardenService.class);
    private final AtomicLong irrigationDurationInSecond = new AtomicLong(90);

    private long soilMaxDryLevelAllowed = 2000;
    private long soilDryLevelPollCount = 30;
    private long soilDryLevelPollFrequencyInMS = 2000;

    private String mediaFileDirectory = "/tmp";
    private Camera camera = null;

    private Sensor<ModbusSoilHumiditySensor.OutputValue> soilHumiditySensor = null;
    private EventDrivenSensor<HallEffectWaterFlowSensor.OutputValue> waterFlowSensor = null;
    private Sensor<INA219VoltageCurrentSensor.OutputValue> voltageCurrentSensor = null;

    private Valve waterValve = null;
    private NotificationService notificationService = null;
    private HealthCheckService healthCheckService = null;
    Map<String, String> config = null;

    public SmartGardenService(DataSource dataSource) throws Throwable {
        // provision gpio pin #01 as an output pin and turn on
        config = this.getConfig(dataSource);
        this.irrigationDurationInSecond.set(Long.parseLong(config.get("irrigation.durationInSeconds")));
        this.soilMaxDryLevelAllowed = soilMaxDryLevelAllowed = Long.parseLong(config.get("soil.maxDryLevelAllowed"));
        this.soilDryLevelPollCount = soilDryLevelPollCount = Long.parseLong(config.get("soil.dryLevelPollCount"));
        this.soilDryLevelPollFrequencyInMS = soilDryLevelPollFrequencyInMS = Long.parseLong(config.get("soil.dryLevelPollFrequencyInMS"));
        this.notificationService = new NotificationService(config.get("notification.email"), config.get("notification.email.password"));
        this.mediaFileDirectory = config.get("monitor.mediaFilePath");
        boolean testing = Boolean.parseBoolean(config.get("general.testing"));

        if (testing) {
            this.camera = new MockCamera();
            this.soilHumiditySensor = new MockPollingSensor<>(new ModbusSoilHumiditySensor.OutputValue(30, 2600));
            this.waterValve = new MockValve();
            this.waterFlowSensor = new MockEventDrivenSensor<>(new HallEffectWaterFlowSensor.OutputValue(BigDecimal.valueOf(299)));
            this.voltageCurrentSensor = new MockPollingSensor<>(new INA219VoltageCurrentSensor.OutputValue(12.0, 3.0));
        } else {
            this.camera = new USBCamera(new Dimension(Integer.parseInt(config.get("image.width")), Integer.parseInt(config.get("image.height"))));
            this.soilHumiditySensor = new ModbusSoilHumiditySensor(config.get("soil.sensorSerialPortName"));
            this.soilHumiditySensor = new MockPollingSensor<>(new ModbusSoilHumiditySensor.OutputValue(30, 2600));
            GpioController gpioController = GpioFactory.getInstance();
            this.waterValve = new WaterValve(gpioController, RaspiPin.GPIO_00);
            this.waterFlowSensor = new HallEffectWaterFlowSensor(gpioController, RaspiPin.GPIO_07);
            I2CBus i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
            this.voltageCurrentSensor = new INA219VoltageCurrentSensor(i2cBus, 0x40);
      //      this.voltageCurrentSensor = new MockPollingSensor<>(new INA219VoltageCurrentSensor.OutputValue(12.0, 3.0));
        }
        this.healthCheckService = new HealthCheckService(this.waterValve, this.voltageCurrentSensor);
    }

    public Map<String, String> getConfig(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> configList = jdbcTemplate.queryForList("select name,value from config");
        Map<String, String> configMap = new HashMap<>();
        for (Map<String, Object> row : configList) {
            configMap.put(row.get("name").toString(), row.get("value").toString());
        }
        return configMap;
    }

    public File getImage(String filename) {
        return new File(config.get("monitor.mediaFilePath"), filename);
    }

    public void healthCheck(DataSource dataSource) {
        if (this.healthCheckService.isValveOpenAfterIrrigation(dataSource, this.irrigationDurationInSecond.get())) {
            this.stopIrrigation(dataSource, null);
        }
        healthCheckService.checkSolarPowerLevel(dataSource);
    }

    public long setIrrigationDuration(long durationInSecond) {
        irrigationDurationInSecond.set(durationInSecond);
        return irrigationDurationInSecond.get();
    }

    public void startIrrigationWhenLowHumidity(final DataSource dataSource) {
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
                this.startIrrigationAsync(dataSource);
            }
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            String sql = "INSERT INTO soil_status (datetime,drylevel,temperature_celsius) values(now(),?,?)";
            jdbcTemplate.update(sql, new Object[]{averageDryLevel, averageTemperature});
        } catch (Throwable t) {
            logger.fatal("startIrrigationWhenLowHumdity", t);
        } finally {

        }

    }

    public Integer startIrrigationAsync(final DataSource dataSource) throws SQLException {
        try {
            if (this.inProgress.get()) {
                return -1;
            }
            this.inProgress.set(true);
            this.waterFlowSensor.startListenEvent();
            this.waterValve.on();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
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
                        self.stopIrrigation(dataSource, historyId);
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

    public boolean stopIrrigation(DataSource dataSource, Integer historyId) {
        if (!inProgress.get()) {
            return true;
        }
        inProgress.set(false);
        String imageFilename = null;
        try {
            this.waterFlowSensor.stopListenEvent();
            imageFilename = UUID.randomUUID().toString() + ".png";
            BufferedImage photoImage = this.camera.takePhoto();
            File imageFile = new File(mediaFileDirectory, imageFilename);
            ImageIO.write(photoImage, "PNG", imageFile);
            if (!imageFile.exists()) {
                throw new FileNotFoundException(imageFilename);
            }
        } catch (Throwable t) {
            logger.error("take photo", t);
        }
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.update("update irrigation_history set end_time=now(),image_filename=?,water_volume_ml=? where id=? and end_time is null",
                    new Object[]{imageFilename, this.waterFlowSensor.readOutputValue().getTotalMillilitre(), historyId});
        } catch (Throwable t) {
            logger.error("update database", t);
        }
        this.waterValve.off();
        return true;
    }

    public List<SoilStatus> getSoilStatusHistory(DataSource dataSource, int itemCount) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(String.format("select id,drylevel,temperature_celsius,datetime from soil_status GROUP BY DATE(soil_status.datetime) order by id desc limit %s", itemCount),
                new RowMapper<SoilStatus>() {
            @Override
            public SoilStatus mapRow(ResultSet rs, int i) throws SQLException {
                SoilStatus soilStatus = new SoilStatus();
                soilStatus.setId(rs.getLong("id"));
                soilStatus.setDatetime(new Timestamp(rs.getTimestamp("datetime").getTime()));
                ModbusSoilHumiditySensor.OutputValue drylevelAndTemperature = new ModbusSoilHumiditySensor.OutputValue(rs.getInt("temperature_celsius"), rs.getInt("drylevel"));
                soilStatus.setDryLevelAndTemperature(drylevelAndTemperature);
                return soilStatus;
            }

        }, new Object[]{});
    }

    public List<PowerStatus> getPowerStatusHistory(DataSource dataSource, int itemCount) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(String.format("select id,voltage,current_ma,datetime from power_status order by id desc limit %s", itemCount),
                new RowMapper<PowerStatus>() {
            @Override
            public PowerStatus mapRow(ResultSet rs, int i) throws SQLException {
                PowerStatus powerStatus = new PowerStatus();
                powerStatus.setId(rs.getInt("id"));
                powerStatus.setDatetime(new Timestamp(rs.getTimestamp("datetime").getTime()));
                INA219VoltageCurrentSensor.OutputValue voltageAndCurrent
                        = new INA219VoltageCurrentSensor.OutputValue(rs.getDouble("voltage"), rs.getDouble("current_ma"));
                powerStatus.setVoltageAndCurrent(voltageAndCurrent);
                return powerStatus;
            }

        }, new Object[]{});
    }

    public IrrigationHistory getLastIrrigationHistory(DataSource dataSource) {
        List<IrrigationHistory> irrigationHistoryList = getIrrigationHistoryList(dataSource, 1);
        if (irrigationHistoryList.isEmpty()) {
            return new IrrigationHistory();
        }
        return irrigationHistoryList.get(0);
    }

    public List<IrrigationHistory> getIrrigationHistoryList(DataSource dataSource, int itemCount) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(String.format("select id,start_time,end_time,image_filename,water_volume_ml from irrigation_history order by id desc limit %s", itemCount),
                new RowMapper<IrrigationHistory>() {
            @Override
            public IrrigationHistory mapRow(ResultSet rs, int i) throws SQLException {
                IrrigationHistory history = new IrrigationHistory();
                history.setId(rs.getInt("id"));
                history.setStartTime(new Date(rs.getTimestamp("start_time").getTime()));
                if (rs.getTimestamp("end_time") != null) {
                    history.setEndTime(new Date(rs.getTimestamp("end_time").getTime()));
                }
                history.setImageFilename(rs.getString("image_filename"));
                history.setWaterVolumeInML(rs.getInt("water_volume_ml"));
                return history;
            }

        }, new Object[]{});
    }

    public GardenStatus getGardenStatus(DataSource dataSource) throws Throwable {
        GardenStatus gardenStatus = new GardenStatus();
        gardenStatus.setConfig(this.getConfig(dataSource));

        PowerStatus powerStatus = new PowerStatus();
        powerStatus.setVoltageAndCurrent(this.voltageCurrentSensor.readOutputValue());
        powerStatus.setBatteryLevelInPercent((int) calculatePowerLevelInPercent(powerStatus.getVoltageAndCurrent().getVoltage()));
        gardenStatus.setPowerStatus(powerStatus);

        gardenStatus.setIrrigationStatus(this.getIrrigationStatus());

        return gardenStatus;
    }

    private IrrigationStatus getIrrigationStatus() throws Throwable {

        IrrigationStatus irrigationStatus = new IrrigationStatus();
        SoilStatus soilStatus = new SoilStatus();
        soilStatus.setDryLevelAndTemperature(this.soilHumiditySensor.readOutputValue());
        irrigationStatus.setSoilStatus(soilStatus);

        irrigationStatus.setWaterValveOpen(this.waterValve.getStatus() == Valve.Status.On);

        WaterFlow waterFlow = new WaterFlow();
        waterFlow.setMilliliter(this.waterFlowSensor.readOutputValue().getTotalMillilitre());
        irrigationStatus.setWaterflow(waterFlow);

        return irrigationStatus;
    }

}
