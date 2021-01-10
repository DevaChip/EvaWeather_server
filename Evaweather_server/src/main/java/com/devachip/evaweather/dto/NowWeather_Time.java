package com.devachip.evaweather.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NowWeather_Time {
	private String date; // 날짜
	private float t0;	 // 0시 온도
	private float t3;	// 3시 온도
	private float t6;	// 6시 온도
	private float t9;	// 9시 온도
	private float t12;	// 12시 온도
	private float t15;	// 15시 온도
	private float t18;	// 18시 온도
	private float t21;	// 21시 온도
}
