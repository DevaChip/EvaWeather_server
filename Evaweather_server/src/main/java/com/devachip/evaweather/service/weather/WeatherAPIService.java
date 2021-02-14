package com.devachip.evaweather.service.weather;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.devachip.evaweather.bean.DataBean;
import com.devachip.evaweather.dto.weatherapi.NowWeather;
import com.devachip.evaweather.dto.weatherapi.NowWeather_AirCondition;
import com.devachip.evaweather.dto.weatherapi.NowWeather_DayInfo;
import com.devachip.evaweather.dto.weatherapi.NowWeather_Detail;
import com.devachip.evaweather.dto.weatherapi.VilageFcstRequest;
import com.devachip.evaweather.persistence.NowWeatherDAO;
import com.devachip.evaweather.vo.LocationInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 날씨 API 받아오기
 * 
 * @author idean
 * @since 2020.11.26
 */
@Slf4j
@Service
public class WeatherAPIService {
	private DataBean dataBean;
	private NowWeatherDAO dao;
	
	// Spring 4.3 이상부터 생성자 주입을 사용할 경우, Autowired 어느테이션을 사용하지 않아도 자동 주입된다.
	public WeatherAPIService(DataBean dataBean, NowWeatherDAO dao) {
		this.dataBean = dataBean;
		this.dao = dao;
	}
	
	public String getNowWeather(String areaCode) {
		// 현재 시간
		DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
		DateFormat tFormat = new SimpleDateFormat("HHmm");
		Date d = new Date();
		String currentDate = dFormat.format(d); 
		String currentTime = tFormat.format(d);
		
		// 현재시간 기준 가장 최근 정시를 기준으로 조회 
		String timePtn = "([0-1]{1}[0-9]{1}|2[0-3]{1})00";
		if (!currentTime.matches(timePtn)) {
			currentTime = StringUtils.substring(currentTime, 0,2) + "00";
		}
		
		// 행정구역코드에 맞는 좌표 정보
		LocationInfo locationInfo = dataBean.getLocationInfoMap().get(areaCode);
		
		VilageFcstRequest request = new VilageFcstRequest();
		request.setBaseDate(currentDate);
		request.setBaseTime(currentTime);
		request.setNx(locationInfo.getNx());
		request.setNy(locationInfo.getNy());
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			String locationName = StringUtils.join(
					new String[] { locationInfo.getFirstArea(), locationInfo.getSecondArea(), locationInfo.getThirdArea() }, " ")
					.trim();
			
			/**
			 * 강수확률 기준 시간
			 * 
			 * 00시부터 3시간마다 강수확률이 존재하기 때문에 매 시간 강수확률을 표현하기가 애매함.
			 * 현재시간 포함 가장 최근의 강수확률을 보여주도록 함.
			 * 
			 * 예) 01시 -> 00시 강수확률 조회
			 *    03시 -> 03시 강수확률 조회
			 */
			String[] popTimes = {"2100", "1800", "1500", "1200", "0900", "0600", "0300", "0000"};
			for (String popTime : popTimes) {
				if (StringUtils.compare(currentTime, popTime) >= 0) {
					request.setPopTime(popTime);
					break;
				}
			}
			
			// 데이터 세팅
			NowWeather dto = dao.getData(request);

			/**
			 * 어제, 오늘, 내일 시간에 대한 온도 세팅
			 * 
			 * 한줄로 출력하기 애매하여 따로 구하기로 함
			 */
			List<NowWeather_DayInfo> timeList = dao.getDaysInfo(request);
			dto.setDaysInfo(timeList);
			
			// 행정구역코드, 지역이름 세팅
			dto.setAreaCode(areaCode);
			dto.setLocationName(locationName);
			
			// 모자란 부분 샘플로 채우기(테스트용)
			setSample(dto);
			
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
		} catch (IOException e) {
			log.error(e.fillInStackTrace() + "");
		} 
		
		return "{\"error\" : \"data parsing Error.\"}";
	}
	
	private boolean setSample(NowWeather dto) {
		try {
			// 대기상태
			NowWeather_AirCondition airCondition = new NowWeather_AirCondition();
			airCondition.setPm10(30);
			airCondition.setPm25(60);
			airCondition.setPm10Grade(1);
			airCondition.setPm25Grade(1);
			airCondition.setO3((float) 0.043);
			airCondition.setO3Grade(1);
			
			dto.setAirCondition(airCondition);
			
			// 상세정보
			NowWeather_Detail detail = dto.getDetail();
			detail.setSunRise("074648");
			detail.setSunSet("172357");
			detail.setKhaiGrade(1);
			detail.setSo2Grade(1);
			detail.setCoGrade(1);
			detail.setNo2Grade(1);
			
			return true;
		} catch(Exception e) {
			log.error(e.fillInStackTrace() + "");
		}
		
		return false;
	}
}