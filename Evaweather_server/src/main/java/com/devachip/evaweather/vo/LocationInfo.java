package com.devachip.evaweather.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 행정구역코드 별 위치 정보를 담는 객체
 * 
 * 동네예보, 동네예보 통보문, 중기예보 API 호출 시 필요함 
 * 
 * @author idean
 * @since 2020.10
 */
@Getter
@AllArgsConstructor
public class LocationInfo {
	String lang;	// 구분
	String areaCode;	// 행정구역코드
	
	String firstArea;	// 1단계 
	String secondArea;	// 2단계
	String thirdArea;	// 3단계
	
	String nx;		// 격자 X
	String ny;		// 격자 Y
	
	String longitude_h;	// 경도(시)
	String longitude_m;	// 경도(분)
	String longitude_s;	// 경도(초)
	
	String latitude_h;		// 위도(시)
	String latitude_m;		// 위도(분)
	String latitude_s;	// 위도(초)
	
	String longitude_s_perHundred;	// 경도 (초/100)
	String latitude_s_perHundred;	// 위도 (초/100)
	
	String locationUpdate;	// 위치업데이트	
}
