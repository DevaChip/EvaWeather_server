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
public class AreaLocation {
	String code;	// 행정구역코드
	
	String firArea;	// 1단계 
	String secArea;	// 2단계
	String thiArea;	// 3단계
	
	int nx;		// 격자 X
	int ny;		// 격자 Y
	
	int longitude_h;	// 경도(시)
	int longitude_m;	// 경도(분)
	double longitude_s;	// 경도(초)
	
	int latitude_h;		// 위도(시)
	int latitude_m;		// 위도(분)
	double latitude_s;	// 위도(초)
	
	double longitude_s_perHundred;	// 경도 (초/100)
	double latitude_s_perHundred;	// 위도 (초/100)
	
	String locationUpdate;	// 위치업데이트	
}
