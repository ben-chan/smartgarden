/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.service;

import org.codemine.iot.device.sensor.EventDrivenSensor;
import org.codemine.iot.device.sensor.HallEffectWaterFlowSensor;
import org.codemine.iot.device.sensor.INA219VoltageCurrentSensor;
import org.codemine.iot.device.sensor.Sensor;
import org.codemine.iot.device.valve.Valve;
import org.codemine.iot.device.valve.WaterValve;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codemine.smartgarden.model.IrrigationHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 *
 * @author BENCPCHAN
 */
@Service
public class HealthCheckService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Logger logger = Logger
            .getLogger(HealthCheckService.class);
    private final Valve waterValve;
    private final Sensor<INA219VoltageCurrentSensor.OutputValue> voltageAndCurrentSensor;
    private final EventDrivenSensor<HallEffectWaterFlowSensor.OutputValue> waterFlowSensor;

    public HealthCheckService(
            Valve waterValve,
            Sensor<INA219VoltageCurrentSensor.OutputValue> voltageAndCurrentSensor,
            EventDrivenSensor<HallEffectWaterFlowSensor.OutputValue> waterFlowSensor) {
        this.waterValve = waterValve;
        this.voltageAndCurrentSensor = voltageAndCurrentSensor;
        this.waterFlowSensor = waterFlowSensor;
    }

    public void checkWaterflow()
            throws DataAccessException, Throwable {
        this.waterFlowSensor.startListenEvent();
        Thread.sleep(5000);
        this.waterFlowSensor.stopListenEvent();
        String sql = "INSERT INTO waterflow_status (date_time,volume_in_ml) values(now(),?)";
        BigDecimal waterVolumeInML = this.waterFlowSensor.readOutputValue().getTotalMillilitre();
        jdbcTemplate.update(sql, new Object[]{waterVolumeInML.intValue()});
        logger.info(String.format("Waterflow=%sML", waterVolumeInML));
    }

    public void checkSolarPowerLevel() {
        try {
            INA219VoltageCurrentSensor.OutputValue voltageAndCurrent = this.voltageAndCurrentSensor
                    .readOutputValue();
            String sql = "INSERT INTO power_status (date_time,voltage,current_in_ma) values(now(),?,?)";
            double voltage = voltageAndCurrent.getVoltage();
            double currentInMA = voltageAndCurrent.getCurrentInMA();
            jdbcTemplate.update(sql,
                    new Object[]{voltage,
                        currentInMA});
            logger.info(String.format("voltage=%s,currentInMA=%s", voltage, currentInMA));
        } catch (Throwable t) {
            logger.error("checkSolarPowerLevel", t);
        }
    }

    public boolean isValveOpenAfterIrrigation(long irrigationDurationInSecond) {
        try {
            logger.log(Level.INFO, "isValveOpenAfterIrrigation started");
            if (this.waterValve.getStatus() == WaterValve.Status.Off) {
                logger.log(Level.INFO,
                        "isValveOpenAfterIrrigation valve is off");
                return false;
            }
            List<IrrigationHistory> historyList = jdbcTemplate
                    .query("select id,start_time,end_time from irrigation_history order by id desc limit 1",
                            new RowMapper<IrrigationHistory>() {
                        @Override
                        public IrrigationHistory mapRow(ResultSet rs,
                                int i) throws SQLException {
                            IrrigationHistory history = new IrrigationHistory();
                            history.setId(rs.getLong("id"));
                            history.setStartTime(new Date(rs
                                    .getTimestamp("start_time")
                                    .getTime()));
                            if (rs.getTimestamp("end_time") != null) {
                                history.setEndTime(new Date(rs
                                        .getTimestamp("end_time")
                                        .getTime()));
                            }
                            return history;
                        }

                    }, new Object[]{});

            if (historyList.isEmpty()) {
                logger.log(Level.INFO, "irrigation history empty");
                return true;
            }
            IrrigationHistory latestHistory = historyList.get(0);
            if (latestHistory.getEndTime() != null) {
                logger.log(Level.INFO, String.format(
                        "isValveOpenAfterIrrigation endtime!=null %s",
                        latestHistory.getEndTime().toString()));
                return true;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(latestHistory.getStartTime());
            final long offsetInSecond = 60;
            calendar.add(Calendar.SECOND,
                    (int) (irrigationDurationInSecond + offsetInSecond));
            Date expectedStopTime = calendar.getTime();
            Date now = new Date();
            if (now.after(expectedStopTime)) {
                logger.log(Level.INFO, String.format(
                        "now %s > expectedStopTime %s healthCheck",
                        now.toString(), expectedStopTime.toString()));
                return true;
            }
            logger.log(Level.INFO, "isValveOpenAfterIrrigation finished");
            return true;
        } catch (Throwable t) {
            logger.error("isValveOpenAfterIrrigation", t);
        }
        return true;
    }
}
