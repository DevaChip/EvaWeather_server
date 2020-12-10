package com.devachip.evaweather.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devachip.evaweather.model.DataBean;
import com.devachip.evaweather.model.UltraSrtNcstRequest;
import com.devachip.evaweather.service.WeatherAPIService;
import com.devachip.evaweather.vo.AreaLocation;

@RestController
public class WeatherAPIController {
	private static final String SERVICE_KEY = "5U%2F51omK%2FH%2F1Qf3TZG9f0QkCSHP9fpI9cAWdjV3xScZ6Sj9QFn4WL7pe8YldzB%2BZjrD1fVBrbNTS2pMDj6siAw%3D%3D";

	private WeatherAPIService service;

	@Autowired
	public WeatherAPIController(WeatherAPIService service) {
		this.service = service;
	}

	@GetMapping(value = "nowWeather")
	public String getVilageFcstInfo(@RequestParam(required=true) String areaCode) {
		// 입력값 검증
		Map<String, Object> reqMap = new HashMap<>();
		reqMap.put("areaCode", areaCode);
		String errorMsg = service.nowWeatherValidation(reqMap);
		
		if (StringUtils.isNotBlank(errorMsg)) {
			return errorMsg;
		}
		
		// 현재 시간
		DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
		DateFormat tFormat = new SimpleDateFormat("hhmm");
		Date d = new Date();
		String currentDate = dFormat.format(d); 
		String currentTime = tFormat.format(d);
		
		// 현재시간이 40분 이전이라면 API 데이터가 없기 때문에 이전 시간 데이터를 호출한다.
		String timePtn = "([0-1]{1}[0-9]{1}|2[0-3]{1})[0-3]{1}[0-9]{1}";
		if (currentTime.matches(timePtn)) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			cal.set(Calendar.MINUTE, -40);
			
			currentDate = dFormat.format(cal.getTime());
			currentTime = tFormat.format(cal.getTime());
		}
		
		// 행정구역코드에 맞는 좌표 정보
		AreaLocation areaLocation = DataBean.getAreaLocations().get(areaCode);
		
		// 필수 입력값 설정
		UltraSrtNcstRequest request = new UltraSrtNcstRequest(SERVICE_KEY, "1", "164", currentDate, currentTime);

		// 옵션값 입력
		request.setDataType("JSON");
		request.setNx(Optional.ofNullable(areaLocation.getNx()).orElse(60));
		request.setNy(Optional.ofNullable(areaLocation.getNy()).orElse(127));

		// API 통신
		String result = service.getUltraSrtNcst(request);
		return result;
	}
}
