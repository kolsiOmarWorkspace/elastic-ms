package com.example.elasticms.controller;


import com.example.elasticms.service.MapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "resource")
public class MapResources {

    @Autowired
    MapperService mapperService;

    @RequestMapping(path = "/read_directories", method = RequestMethod.GET)
    public void readDirectories(@RequestParam String... resources) {
        mapperService.readDirectories(resources);
    }

}
