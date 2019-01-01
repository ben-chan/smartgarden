/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.util;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import java.awt.Dimension;
import org.codemine.iot.device.camera.Camera;
import org.codemine.iot.device.camera.USBCamera;
import org.codemine.iot.device.sensor.EventDrivenSensor;
import org.codemine.iot.device.sensor.HallEffectWaterFlowSensor;
import org.codemine.iot.device.sensor.INA219VoltageCurrentSensor;
import org.codemine.iot.device.sensor.ModbusSoilHumiditySensor;
import org.codemine.iot.device.sensor.Sensor;
import org.codemine.iot.device.valve.Valve;
import org.codemine.iot.device.valve.WaterValve;
import org.codemine.iot.io.MockGpioController;
import org.codemine.smartgarden.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author demof
 */
@Component
@Profile("prod")
public class DeviceFactory {

    @Autowired
    private ConfigService configService;

    @Bean
    public Camera camera() {
        return new USBCamera(new Dimension(configService.getValue("image.width", Integer::parseInt), configService.getValue("image.height", Integer::parseInt)));
    }

    @Bean
    public Valve waterValve(GpioController gpioController) {
        return new WaterValve(gpioController, RaspiPin.GPIO_00);
    }

    @Bean
    public Sensor<ModbusSoilHumiditySensor.OutputValue> soilHumiditySensor() throws Throwable {
        return new ModbusSoilHumiditySensor(configService.getValue("soil.sensorSerialPortName"));
    }

    @Bean
    public Sensor<INA219VoltageCurrentSensor.OutputValue> voltageCurrentSensor() throws Throwable {
        I2CBus i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
        return new INA219VoltageCurrentSensor(i2cBus, 0x40);
    }

    @Bean
    public EventDrivenSensor<HallEffectWaterFlowSensor.OutputValue> waterFlowSensor() {
        return new HallEffectWaterFlowSensor(new MockGpioController(), RaspiPin.GPIO_25);
    }

    @Bean
    public GpioController gpioController() {
        return GpioFactory.getInstance();
    }

}
