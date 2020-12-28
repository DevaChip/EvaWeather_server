package com.devachip.evaweather.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devachip.evaweather.service.WeatherAPIService;

@RestController
public class WeatherAPIController {
	private WeatherAPIService service;

	@Autowired
	public WeatherAPIController(WeatherAPIService service) {
		this.service = service;
	}

	@GetMapping(value = "nowWeather")
	public String getVilageFcstInfo(@RequestParam(required=true) String areaCode,
									@RequestParam(required=false) String time) {
		String nowWeatherJson = service.getNowWeather(areaCode, time); 
		
		return nowWeatherJson;
	}
}
