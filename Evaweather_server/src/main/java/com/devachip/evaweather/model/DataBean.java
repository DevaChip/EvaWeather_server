package com.devachip.evaweather.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.devachip.evaweather.vo.AreaLocation;


/**
 * API 조회에 필요한 데이터를 담고 있는 클래스
 * Spring Bean 등록하여 싱글톤으로 관리되도록 한다.
 * 
 * 1. 행정구역코드 및 지역 정보(동네예보, 동네예보 통보문, 중기예보 API 조회 시, 사용됨.)
 * 
 * @author idean
 * @since 2020.12.10
 */
@Component
public class DataBean {
	private static Map<String, AreaLocation> areaLocations;
	
	public DataBean() {
		init();
	}
	
	public static Map<String, AreaLocation> getAreaLocations() {
		return areaLocations;
	}
	
	private void init() {
		if (areaLocations==null) {	// 최초 한번만 읽어오도록
			areaLocations = new HashMap<>();
			
			// 2020.12.10 샘플데이터 삽입
			// TODO: 엑셀파일에서 값을 읽어 추가
			String code = "1100000000";
			
			String firArea = "서울특별시";
			String secArea = "";
			String thiArea = "";
			
			int nx = 60;
			int ny = 127;
			
			int longitude_h = 126;
			int longitude_m = 58;
			double longitude_s = 48.03;
			
			int latitude_h = 37;
			int latitude_m = 33;
			double latitude_s = 48.85;
			
			double longitude_s_perHundred = 126.98000833333333;
			double latitude_s_perHundred = 37.56356944444444;
			String locationUpdate = "";
			
			AreaLocation krAreaCode = new AreaLocation(code, firArea, secArea, thiArea
													, nx, ny, longitude_h, longitude_m, longitude_s
													, latitude_h, latitude_m, latitude_s
													, longitude_s_perHundred, latitude_s_perHundred
													, locationUpdate);
			areaLocations.put(code, krAreaCode);
		}
	}
}
