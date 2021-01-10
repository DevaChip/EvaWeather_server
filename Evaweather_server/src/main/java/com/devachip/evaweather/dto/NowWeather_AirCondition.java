package com.devachip.evaweather.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NowWeather_AirCondition {
	private float pm10;	// 미세먼지
	private float pm25;	// 초미세먼지
	private float pm10Grade;	// 미세먼지 지수
	private float pm25Grade;	// 초미세먼지 지수
	private float o3;	// 오존농도
	private float o3Grade;	// 오존농도 지수
}
