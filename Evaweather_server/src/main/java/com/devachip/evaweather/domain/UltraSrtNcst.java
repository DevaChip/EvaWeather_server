package com.devachip.evaweather.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UltraSrtNcst {
	private String tableName = "UltraSrtNcsts";
	
	final private String baseDate;
	final private String baseTime;
	final private int nx;
	final private int ny;
	
	final private float T1H;	// 기온 | 섭씨
	final private float RN1;	// 1시간 강수량
	final private float UUU;	// 동서 바람성분 | 동(+표기), 서(-표기)
	final private float VVV;	// 남북 바람성분 | 북(+표기), 남(-표기)
	final private float REH;	// 습도 | %
	final private float PTY;	// 강수형태 | 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4), 빗방울(5), 빗방울/눈날림(6), 눈날림(7)
	final private float VEC;	// 풍향 | (풍향값 + 22.5 * 0.5) / 22.5) = 변환값(소수점 이하 버림)
	final private float WSD;	// 풍속 | m/s
}
