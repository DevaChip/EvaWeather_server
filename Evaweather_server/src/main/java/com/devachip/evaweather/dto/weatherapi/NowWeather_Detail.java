package com.devachip.evaweather.dto.weatherapi;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NowWeather_Detail {
	private String sunRise;	// 일출
	private String sunSet;	// 일몰
	private float sd;	// 습도(REH)
	private float windSpeed;	// 풍속(WSD)
	private String windVector;	// 풍향(VEC)
	private float khaiGrade;	// 동합대기환경지수
	private float so2Grade;	// 아황산가스 지수
	private float coGrade;	// 일산화탄소 지수
	private float no2Grade;	// 이산화질소 지수
}
