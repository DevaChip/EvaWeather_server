package com.devachip.evaweather.model;

import org.springframework.lang.Nullable;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class UltraSrtNcstRequest {
	/* 필수 */
	@NonNull private String serviceKey;	// 서비스키
	@NonNull private String pageNo;		// 페이지 번호
	@NonNull private String numOfRows;	// 한 페이지 결과 수
	@NonNull private String baseDate;	// 발표일자[yyyyMMdd]
	@NonNull private String baseTime;	// 발표시작[hhmm]
	
	/* 옵션 */
	@Nullable private String dataType;// 응답자료형식[ XML | JSON ]
	@Nullable private int nx;		// 예보지점 X 좌표[엑셀 참고]
	@Nullable private int ny;		// 예보지점 Y 좌표[엑셀 참고]
}