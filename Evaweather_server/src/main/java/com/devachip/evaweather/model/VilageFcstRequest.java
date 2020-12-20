package com.devachip.evaweather.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VilageFcstRequest {
	/* 필수 */
	private String pageNo;		// 페이지 번호
	private String numOfRows;	// 한 페이지 결과 수
	private String baseDate;	// 발표일자[yyyyMMdd]
	private String baseTime;	// 발표시작[hhmm]
	
	/* 옵션 */
	private String dataType;// 응답자료형식[ XML | JSON ]
	private String nx;		// 예보지점 X 좌표[엑셀 참고]
	private String ny;		// 예보지점 Y 좌표[엑셀 참고]
}