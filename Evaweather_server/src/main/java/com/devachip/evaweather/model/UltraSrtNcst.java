package com.devachip.evaweather.model;

import lombok.Getter;

@Getter
public class UltraSrtNcst {
	private String baseDate;
	private String baseTime;
	private int nx;
	private int ny;
	private String T1H;	// 기온 | 섭씨
	private String RN1;	// 1시간 강수량
	private String UUU;	// 동서 바람성분 | 동(+표기), 서(-표기)
	private String VVV;	// 남북 바람성분 | 북(+표기), 남(-표기)
	private String REH;	// 습도 | %
	private String PTY;	// 강수형태 | 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4), 빗방울(5), 빗방울/눈날림(6), 눈날림(7)
	private String VEC;	// 풍향 | (풍향값 + 22.5 * 0.5) / 22.5) = 변환값(소수점 이하 버림)
	private String WSD;	// 풍속 | m/s
}
