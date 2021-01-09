package com.devachip.evaweather.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devachip.evaweather.service.WeatherAPIService;

import io.swagger.annotations.ApiOperation;

/**
 * 날씨 조회 컨트롤러
 */
@RestController
public class WeatherAPIController {
	private WeatherAPIService service;

	@Autowired
	public WeatherAPIController(WeatherAPIService service) {
		this.service = service;
	}

	@ApiOperation(value="get NowWeather", notes="특정 위치의 현재 날씨 조회")
	@GetMapping(value = "nowWeather", produces="application/json;charset=UTF-8")
	public String getVilageFcstInfo(@RequestParam(required=true) String areaCode,
									@RequestParam(required=false) String date,
									@RequestParam(required=false) String time) {
		String nowWeatherJson = service.getNowWeather(areaCode, date, time); 
		
		return nowWeatherJson;
	}
}
