package com.devachip.evaweather.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NowWeather {
	private String areaCode;	// 행정구역코드
	private String locationName;// 지역 이름
	private float currentTemperature;// 현재 기온(T1H)
	private float maxTemperature;	// 낮 최고기온
	private float minTemperature;	// 아침 최저기온
	private float pop;	// 강수확률
	private float sky;	// 하늘상태(SKY)
	private float pty;	// 강수형태(PTY)
	private float rn1;	// 1시간 강수량(RN1)
	private float lgt;	// 낙뢰정보(LGT)
	
	private NowWeather_AirCondition airCondition;	// 대기상태
	private List<NowWeather_DayInfo> daysInfo;	// 어제, 오늘, 내일 기온
	private NowWeather_Detail detail;	// 상세정보
	
	private List<NowWeather_Clothes> clothes;	// 옷 정보
}
