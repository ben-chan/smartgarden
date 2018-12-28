/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.repository;

import org.codemine.smartgarden.model.Config;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author demof
 */
public interface ConfigRepository extends JpaRepository<Config,Long>{
    Config findByName(String name);
}
