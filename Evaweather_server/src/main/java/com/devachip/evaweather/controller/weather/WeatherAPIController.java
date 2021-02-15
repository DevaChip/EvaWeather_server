package com.devachip.evaweather.controller.weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devachip.evaweather.service.weather.WeatherAPIService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 날씨 조회 컨트롤러
 */
@RestController
@RequestMapping("/weather")
public class WeatherAPIController {
	private WeatherAPIService service;

	@Autowired
	public WeatherAPIController(WeatherAPIService service) {
		this.service = service;
	}

	@ApiOperation(value = "get Weather", notes = "특정 위치의 현재 날씨 및 옷 정보 조회")
	@GetMapping(value = "/now", produces = "application/json;charset=UTF-8")
	public String getVilageFcstInfo(
			@ApiParam("행정구역코드") @RequestParam(required = true) String areaCode) {
		return service.getNowWeather(areaCode);
	}
}
