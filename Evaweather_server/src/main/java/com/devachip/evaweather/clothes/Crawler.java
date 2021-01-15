package com.devachip.evaweather.clothes;

import java.util.List;
import java.util.Map;

import com.devachip.evaweather.dto.NowWeather_Clothes;

/**
 * 크롤링을 위한 인터페이스 구현
 * 
 * @author dykim
 * @since 2021.01.13
 */
public interface Crawler {
	public Map<String, Object> getItemList();
	public List<NowWeather_Clothes> getClothes(String gender, String season);
}
