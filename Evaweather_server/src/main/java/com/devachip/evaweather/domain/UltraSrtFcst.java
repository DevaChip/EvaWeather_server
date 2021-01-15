package com.devachip.evaweather.domain;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UltraSrtFcst {
	private String tableName = "UltraSrtFcsts";
	
	@NonNull private String fcstDate;
	@NonNull private String fcstTime;
	@NonNull private int nx;
	@NonNull private int ny;
	
	@NonNull private float T1H;	// 기온 | 섭씨
	@NonNull private float RN1;	// 1시간 강수량
	@NonNull private float SKY;	// 하늘상태 | 맑음(1), 구름많음(3), 흐림(4)
	@NonNull private float UUU;	// 동서 바람성분 | 동(+표기), 서(-표기)
	@NonNull private float VVV;	// 남북 바람성분 | 북(+표기), 남(-표기)
	@NonNull private float REH;	// 습도 | %
	@NonNull private float PTY;	// 강수형태 | 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4), 빗방울(5), 빗방울/눈날림(6), 눈날림(7)
	@NonNull private float LGT;	// 낙뢰
	@NonNull private float VEC;	// 풍향 | (풍향값 + 22.5 * 0.5) / 22.5) = 변환값(소수점 이하 버림)
	@NonNull private float WSD;	// 풍속 | m/s	
}
