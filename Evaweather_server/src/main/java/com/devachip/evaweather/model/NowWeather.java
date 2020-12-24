package com.devachip.evaweather.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NowWeather {
	
	/** 초단기실황 */
	private String areaCode;	// 행정구역코드
	private String locationName;// 지역 이름
	private float currentTemperature;// 현재 기온(T1H)
	private float pty;	// 강수형태(PTY)
	private float rn1;	// 1시간 강수량(RN1)
	private float sd;	// 습도(REH)
	private float windSpeed;	// 풍속(WSD)
	private String windVector;	// 풍향(VEC)
}
