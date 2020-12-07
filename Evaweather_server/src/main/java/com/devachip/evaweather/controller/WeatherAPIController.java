package com.devachip.evaweather.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devachip.evaweather.model.UltraSrtNcstRequest;
import com.devachip.evaweather.service.WeatherAPIService;

@RestController
public class WeatherAPIController {
	private static final String SERVICE_KEY = "5U%2F51omK%2FH%2F1Qf3TZG9f0QkCSHP9fpI9cAWdjV3xScZ6Sj9QFn4WL7pe8YldzB%2BZjrD1fVBrbNTS2pMDj6siAw%3D%3D";

	private WeatherAPIService service;

	@Autowired
	public WeatherAPIController(WeatherAPIService service) {
		this.service = service;
	}

	@GetMapping(value = "nowWeather")
	public String getVliageFcstInfo(HttpServletRequest req) {
		// 입력값 검증
		String errorMsg = service.nowWeatherValidation(req);
		
		if (StringUtils.isNotBlank(errorMsg)) {
			return errorMsg;
		}
		
		DateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date d = new Date();
		String currentDate = format.format(d); 
		
		// 필수 입력값 설정
		UltraSrtNcstRequest request = new UltraSrtNcstRequest(
				SERVICE_KEY, 
				getParameter(req, "pageNo", "1"),
				getParameter(req, "numOfRows", "10"), 
				getParameter(req, "baseDate", currentDate),
				getParameter(req, "baseTime", "0000")
				);

		// 옵션값 입력
		request.setDataType(getParameter(req, "dataType", "JSON"));
		request.setNx(getParameter(req, "nx", "60"));
		request.setNy(getParameter(req, "ny", "127"));

		String result = service.getUltraSrtNcst(request);

		return result;
	}
	
	public String getParameter(HttpServletRequest req, String key, String defaultValue) {
		String returnValue = "";
		
		try {
			String value = req.getParameter(key);
			
			if (StringUtils.isBlank(value)) {
				returnValue = defaultValue;
			} else {
				returnValue = value;
			}
		} catch (Exception e) {
			e.fillInStackTrace();
			System.out.println(e);
		}
		
		return returnValue;
	}
}
