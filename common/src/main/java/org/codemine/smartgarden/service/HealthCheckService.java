/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.service;

import com.codemine.iot.device.sensor.INA219VoltageCurrentSensor;
import com.codemine.iot.device.sensor.Sensor;
import com.codemine.iot.device.valve.Valve;
import com.codemine.iot.device.valve.WaterValve;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codemine.smartgarden.model.IrrigationHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author BENCPCHAN
 */
public class HealthCheckService {
    
    private static final Logger logger = Logger.getLogger(HealthCheckService.class);
    private final Valve waterValve;
    private Sensor<INA219VoltageCurrentSensor.OutputValue> voltageAndCurrentSensor;
    
    public HealthCheckService(Valve waterValve, Sensor<INA219VoltageCurrentSensor.OutputValue> voltageAndCurrentSensor) {
        this.waterValve = waterValve;
        this.voltageAndCurrentSensor = voltageAndCurrentSensor;
    }
    
    public void checkSolarPowerLevel(DataSource dataSource) {
        try {
            INA219VoltageCurrentSensor.OutputValue voltageAndCurrent = this.voltageAndCurrentSensor.readOutputValue();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);            
            String sql = "INSERT INTO power_status (datetime,voltage,current_ma) values(now(),?,?)";            
            jdbcTemplate.update(sql, new Object[]{voltageAndCurrent.getVoltage(), voltageAndCurrent.getCurrentInMA()});            
        } catch (Throwable t) {
            logger.error("checkSolarPowerLevel", t);
        }
    }
    
    public boolean isValveOpenAfterIrrigation(DataSource dataSource, long irrigationDurationInSecond) {
        try {
            logger.log(Level.INFO, "isValveOpenAfterIrrigation started");
            if (this.waterValve.getStatus() == WaterValve.Status.Off) {
                logger.log(Level.INFO, "isValveOpenAfterIrrigation valve is off");
                return false;
            }
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            List<IrrigationHistory> historyList = jdbcTemplate
                    .query("select id,start_time,end_time from irrigation_history order by id desc limit 1", new RowMapper<IrrigationHistory>() {
                        @Override
                        public IrrigationHistory mapRow(ResultSet rs, int i) throws SQLException {
                            IrrigationHistory history = new IrrigationHistory();
                            history.setId(rs.getInt("id"));
                            history.setStartTime(new Date(rs.getTimestamp("start_time").getTime()));
                            if (rs.getTimestamp("end_time") != null) {
                                history.setEndTime(new Date(rs.getTimestamp("end_time").getTime()));
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
                logger.log(Level.INFO,
                        String.format("isValveOpenAfterIrrigation endtime!=null %s", latestHistory.getEndTime().toString()));
                return true;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(latestHistory.getStartTime());
            final long offsetInSecond = 60;
            calendar.add(Calendar.SECOND, (int) (irrigationDurationInSecond + offsetInSecond));
            Date expectedStopTime = calendar.getTime();
            Date now = new Date();
            if (now.after(expectedStopTime)) {
                logger.log(Level.INFO, String.format("now %s > expectedStopTime %s healthCheck", now.toString(),
                        expectedStopTime.toString()));
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