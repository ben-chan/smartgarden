/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.util;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.RaspiPin;
import org.codemine.iot.device.camera.Camera;
import org.codemine.iot.device.camera.MockCamera;
import org.codemine.iot.device.sensor.EventDrivenSensor;
import org.codemine.iot.device.sensor.HallEffectWaterFlowSensor;
import org.codemine.iot.device.sensor.INA219VoltageCurrentSensor;
import org.codemine.iot.device.sensor.MockPollingSensor;
import org.codemine.iot.device.sensor.ModbusSoilHumiditySensor;
import org.codemine.iot.device.sensor.Sensor;
import org.codemine.iot.device.valve.MockValve;
import org.codemine.iot.device.valve.Valve;
import org.codemine.iot.io.MockGpioController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author demof
 */
@Component
@Profile("dev")
public class MockDeviceFactory {

    @Bean
    public Camera camera() {
        return new MockCamera();
    }

    @Bean
    public Valve waterValve() {
        MockValve valve=new MockValve();
        valve.off();
        return valve;
    }

    @Bean
    public Sensor<ModbusSoilHumiditySensor.OutputValue> soilHumiditySensor() {
        return new MockPollingSensor<>(new ModbusSoilHumiditySensor.OutputValue(30, 2600));
    }

    @Bean
    public Sensor<INA219VoltageCurrentSensor.OutputValue> voltageCurrentSensor() {
        return new MockPollingSensor<>(new INA219VoltageCurrentSensor.OutputValue(12.0, 3.0));
    }

    @Bean
    public EventDrivenSensor<HallEffectWaterFlowSensor.OutputValue> waterFlowSensor() {
        return new HallEffectWaterFlowSensor(new MockGpioController(), RaspiPin.GPIO_25);
    }

    @Bean
    public GpioController gpioController() {
        return new MockGpioController();
    }

}
