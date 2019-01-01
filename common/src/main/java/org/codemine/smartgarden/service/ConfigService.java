/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import org.codemine.smartgarden.model.Config;
import org.codemine.smartgarden.repository.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author demof
 */
@Service
public class ConfigService {

    @Autowired
    private ConfigRepository configRepository;

    @Transactional(readOnly = false)
    public void initDefaultConfigIfMissing() {
        this.saveIfNotExist("irrigation.durationInSeconds", Integer.toString(60));
        this.saveIfNotExist("soil.maxDryLevelAllowed", Integer.toString(4000));
        this.saveIfNotExist("soil.dryLevelPollFrequencyInMS", Integer.toString(2000));
        this.saveIfNotExist("irrigation.durationInSeconds", Integer.toString(60));

        this.saveIfNotExist("soil.dryLevelPollCount", Integer.toString(10));

        this.saveIfNotExist("monitor.mediaFilePath", "c:\\temp");
        this.saveIfNotExist("notification.email", "smartgardenhk@gmail.com");
        this.saveIfNotExist("notification.email.password", "Onlun167yu<<>>");

        this.saveIfNotExist("image.width", "1024");
        this.saveIfNotExist("image.height", "768");
        this.saveIfNotExist("soil.sensorSerialPortName", "/dev/ttyS01");
    }

    public String getValue(String name) {
        return this.getValue(name, (String t) -> t);
    }

    public <T> T getValue(String name, Function<String, T> converter) {
        Config config = this.configRepository.findByName(name);
        if (config == null) {
            throw new NoSuchElementException(name);
        }
        return converter.apply(config.getValue());
    }

    private void saveIfNotExist(String name, String value) {
        if (configRepository.findByName(name) == null) {
            Config config = new Config();
            config.setName(name);
            config.setValue(value);
            this.configRepository.save(config);
        }
    }

    public Map<String, String> getAllConfig() {
        Map<String, String> allConfig = new HashMap<>();
        this.configRepository.findAll().stream().forEach(config -> {
            allConfig.put(config.getName(), config.getValue());
        });
        return allConfig;
    }

}
