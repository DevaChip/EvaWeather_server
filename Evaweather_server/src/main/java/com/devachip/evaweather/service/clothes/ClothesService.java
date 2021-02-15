package com.devachip.evaweather.service.clothes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

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
			List<Clothes> clothesList = new ArrayList<>();
			for (String siteName: siteList) {
				crawler = CrawlerFactory.getInstance(siteName);
				
				List<Clothes> crawlingList = crawler.getClothes(gender, season); 
				clothesList.addAll(crawlingList);
			}
			
			response = new ClothesResponse(clothesList);
			
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
		} catch (Exception e) {
			log.error(e.fillInStackTrace() + "");
		}
		
		return null;
	}
	
	private String getSeasonByMonth() {
		Supplier<Integer> month = () -> (Calendar.getInstance().get(Calendar.MONTH) + 1)%12; 
		return Optional.of(month.get())
				.filter(m -> m <3).map(m -> "겨울")
				.or(() -> Optional.of(month.get()).filter(m -> m < 6).map(m -> "봄"))
				.or(() -> Optional.of(month.get()).filter(m -> m < 9).map(m -> "여름"))
				.orElseGet(() ->"가을");
	}
}
