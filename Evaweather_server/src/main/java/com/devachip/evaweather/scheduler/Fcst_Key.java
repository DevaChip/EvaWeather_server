package com.devachip.evaweather.scheduler;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 날씨 조회 API 데이터 전용 다중 키
 * 
 * 조회된 데이터가 여러 값이 하나의 키를 형성하기 때문에 추가함
 * @author dykim
 *
 */
@Getter
@AllArgsConstructor
public class Fcst_Key {
	private String fcstDate;
	private String fcstTime;
	private int nx;
	private int ny;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		
		Fcst_Key key = (Fcst_Key) obj;
		
		return StringUtils.equals(key.fcstDate, this.fcstDate) && StringUtils.equals(key.fcstTime, this.fcstTime) && key.nx==this.nx && key.ny==this.ny;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(fcstDate, fcstTime, nx, ny);
	}
}
