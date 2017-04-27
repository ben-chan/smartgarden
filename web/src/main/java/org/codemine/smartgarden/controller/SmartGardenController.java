/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codemine.smartgarden.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author BENCPCHAN
 */
@Controller
@RequestMapping("/employee/*")
public class SmartGardenController {

    @RequestMapping("/greeting")

    public @ResponseBody
    String greeting(@RequestParam(value = "name", required = false, defaultValue = "World") String name, Model model) {

        return "greeting";
    }
}