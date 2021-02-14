package com.devachip.evaweather.service.clothes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.stereotype.Service;

import com.devachip.evaweather.base.PropertiesConfig;
import com.devachip.evaweather.crawler.Crawler;
import com.devachip.evaweather.crawler.CrawlerFactory;
import com.devachip.evaweather.dto.clothesapi.Clothes;
import com.devachip.evaweather.dto.clothesapi.ClothesResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClothesService {
	
	private PropertiesConfig properties;
	
	public ClothesService(PropertiesConfig properties) {
		this.properties = properties;
	}
	
	/**
	 * 성별, 계절에 맞는 옷 정보 세팅
	 * 
	 * 성별과 계절에 맞는 옷 정보를 서버 내부 파일로부터 읽어온다.
	 * 옷 정보는 크롤링된 데이터를 사용한다.
	 * 
	 * @param dto
	 * @param request
	 * @return boolean [true | false]
	 * 
	 * @author idean
	 * @since 2021.02.14
	 */
	public String getClothes(String gender) {
		ObjectMapper mapper = new ObjectMapper();
		
		ClothesResponse response = null;
		try {
			String[] siteList = properties.getSiteList();
			String season = getSeasonByMonth();
			
			Crawler crawler = null;
			List<Clothes> clothes = new ArrayList<>();
			for (String siteName: siteList) {
				crawler = CrawlerFactory.getInstance(siteName);
				
				List<Clothes> crawlingList = crawler.getClothes(gender, season); 
				clothes.addAll(crawlingList);
			}
			
			response = new ClothesResponse(clothes);
			
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
		} catch (Exception e) {
			log.error(e.fillInStackTrace() + "");
		}
		
		return null;
	}
	
	private String getSeasonByMonth() {
		Calendar cal = Calendar.getInstance();
		
		int month = cal.get(Calendar.MONTH) + 1;
		
		switch(month) {
		case 3: case 4: case 5: 
			return "봄";
		case 6: case 7: case 8: 
			return "여름";
		case 9: case 10:  case 11: 
			return "가을";
		default:
			return "겨울";
		}
	}
}
