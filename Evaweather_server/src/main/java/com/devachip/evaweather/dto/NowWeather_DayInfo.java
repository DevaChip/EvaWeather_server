package com.devachip.evaweather.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NowWeather_DayInfo {
	private String date;	// 날짜
	private NowWeather_DayInfo_TimeWeather t0;	 // 0시 날씨 정보
	private NowWeather_DayInfo_TimeWeather t3;	// 3시 날씨 정보
	private NowWeather_DayInfo_TimeWeather t6;	// 6시 날씨 정보
	private NowWeather_DayInfo_TimeWeather t9;	// 9시 날씨 정보
	private NowWeather_DayInfo_TimeWeather t12;	// 12시 날씨 정보
	private NowWeather_DayInfo_TimeWeather t15;	// 15시 날씨 정보
	private NowWeather_DayInfo_TimeWeather t18;	// 18시 날씨 정보
	private NowWeather_DayInfo_TimeWeather t21;	// 21시 날씨 정보
}
