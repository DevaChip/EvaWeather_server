package com.devachip.evaweather.persistence;

import java.util.List;

import com.devachip.evaweather.dto.NowWeather;
import com.devachip.evaweather.dto.NowWeather_Time;
import com.devachip.evaweather.dto.VilageFcstRequest;

public interface NowWeatherDAO {
	/* 현재 시간에 대한 날씨 정보 */
	public NowWeather getData(VilageFcstRequest request);
	
	/* 어제, 오늘, 내일 시간대별 예상 기온 */
	public List<NowWeather_Time> getTime(VilageFcstRequest request);
	
	public default String test() {	
		return "TEST";
	}
}
