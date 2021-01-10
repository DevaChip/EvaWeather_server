package com.devachip.evaweather.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devachip.evaweather.base.PropertiesConfig;
import com.devachip.evaweather.bean.DataBean;
import com.devachip.evaweather.dto.NowWeather;
import com.devachip.evaweather.dto.NowWeather_AirCondition;
import com.devachip.evaweather.dto.NowWeather_Clothes;
import com.devachip.evaweather.dto.NowWeather_Detail;
import com.devachip.evaweather.dto.NowWeather_Time;
import com.devachip.evaweather.dto.VilageFcstRequest;
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
	
	private PropertiesConfig properties;
	private NowWeatherDAO dao;
	
	// TODO: Autowired 하지 않아도 의존성 주입이 된다.
	// 컨트롤러에서 서비스 코드를 의존성 주입할 경우 하위 생성자에도 의존성 주입이 되는지 확인이 필요하다.
	@Autowired
	public WeatherAPIService(PropertiesConfig properties, NowWeatherDAO dao) {
		this.dao = dao;
		this.properties = properties;
	}
	
	public String getNowWeather(String gender, String areaCode, String date, String time) {
		// 현재 시간
		DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
		DateFormat tFormat = new SimpleDateFormat("HHmm");
		Date d = new Date();
		String currentDate = dFormat.format(d); 
		String currentTime = tFormat.format(d);
		
		// TODO: 테스트용 코드, 최종본에선 삭제할 것
		if (StringUtils.isNotBlank(time)) {
			currentTime = time;
		}
		
		if (StringUtils.isNotBlank(date)) {
			currentDate = date;
		}
		
		// 현재시간 기준 가장 최근 정시를 기준으로 조회 
		String timePtn = "([0-1]{1}[0-9]{1}|2[0-3]{1})00";
		if (!currentTime.matches(timePtn)) {
			currentTime = StringUtils.substring(currentTime, 0,2) + "00";
		}
		
		// 행정구역코드에 맞는 좌표 정보
		LocationInfo locationInfo = DataBean.getLocationInfoMap().get(areaCode);
		
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
			List<NowWeather_Time> timeList = dao.getTime(request);
			dto.setTime(timeList);
			
			// 행정구역코드, 지역이름 세팅
			dto.setAreaCode(areaCode);
			dto.setLocationName(locationName);
			
			// 옷 정보 세팅
			setClothes(dto, request);
			
			// 모자란 부분 샘플로 채우기(테스트용)
			setSample(dto);
			
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
		} catch (IOException e) {
			log.error(e.fillInStackTrace() + "");
		} 
		
		return "{\"error\" : \"data parsing Error.\"}";
	}
	
	/**
	 * 날씨에 맞는 옷 정보 세팅
	 * 
	 * 날씨에 맞는 옷 정보를 서버 내부 파일로부터 읽어온다.
	 * 옷 정보는 크롤링된 데이터를 사용한다.
	 * 
	 * @param dto
	 * @param request
	 * @return boolean [true | false]
	 * 
	 * @author idean
	 * @since 2021.01.10
	 */
	private boolean setClothes(NowWeather dto, VilageFcstRequest request) {
		String basePath = properties.getClothes_path();
		InputStream is  = null;
		byte[] imgBytes = null;
		List<NowWeather_Clothes> clothes = new ArrayList<>();

		try {
			// 아우터
			String outerPath = basePath + File.separator + "outer.jpg";
			
			is = new BufferedInputStream(new FileInputStream(outerPath));
			imgBytes = IOUtils.toByteArray(is);
			
			NowWeather_Clothes outer = new NowWeather_Clothes();
			outer.setImg(imgBytes);
			outer.setLink("TODO");
			
			clothes.add(outer);
			
			// 상의
			String topPath = basePath + File.separator + "top.jpg";
			
			is = new BufferedInputStream(new FileInputStream(topPath));
			imgBytes = IOUtils.toByteArray(is);
			
			NowWeather_Clothes top = new NowWeather_Clothes();
			top.setImg(imgBytes);
			top.setLink("TODO");
			
			clothes.add(top);
			
			// 바지
			String pantsPath = basePath + File.separator + "pants.jpg";
			
			is = new BufferedInputStream(new FileInputStream(pantsPath));
			imgBytes = IOUtils.toByteArray(is);
			
			NowWeather_Clothes pants = new NowWeather_Clothes();
			pants.setImg(imgBytes);
			pants.setLink("TODO");
			
			clothes.add(pants);
			
			// 신발
			String shoosePath = basePath + File.separator + "shoose.jpg";
			
			is = new BufferedInputStream(new FileInputStream(shoosePath));
			imgBytes = IOUtils.toByteArray(is);
			
			NowWeather_Clothes shoose = new NowWeather_Clothes();
			shoose.setImg(imgBytes);
			shoose.setLink("TODO");
			
			clothes.add(shoose);
			
			// 악세서리
			String accessoriesPath = basePath + File.separator + "accessories.jpg";
			
			is = new BufferedInputStream(new FileInputStream(accessoriesPath));
			imgBytes = IOUtils.toByteArray(is);
			
			NowWeather_Clothes accessories = new NowWeather_Clothes();
			accessories.setImg(imgBytes);
			accessories.setLink("TODO");
			
			clothes.add(accessories);
			

			dto.setClothes(clothes);
			return true;
		} catch (FileNotFoundException e) {
			log.error(e.fillInStackTrace() + "");
		} catch (IOException e) {
			log.error(e.fillInStackTrace() + "");
		}
		
		return false;
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
