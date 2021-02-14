package com.devachip.evaweather.persistence;

import java.util.List;

import com.devachip.evaweather.dto.weatherapi.NowWeather;
import com.devachip.evaweather.dto.weatherapi.NowWeather_DayInfo;
import com.devachip.evaweather.dto.weatherapi.VilageFcstRequest;

public interface NowWeatherDAO {
	/* 현재 시간에 대한 날씨 정보 */
	public NowWeather getData(VilageFcstRequest request);
	
	/* 어제, 오늘, 내일 시간대별 예상 기온 */
	public List<NowWeather_DayInfo> getDaysInfo(VilageFcstRequest request);
}
