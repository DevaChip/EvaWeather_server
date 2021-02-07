package com.devachip.evaweather.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VilageFcst {
	public static String tableName = "VilageFcsts";
	
	final private String fcstDate;
	final private String fcstTime;
	final private int nx;
	final private int ny;
	
	final private float POP;	// 강수확률 | %
	final private float PTY;	// 강수형태 | 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4), 빗방울(5), 빗방울/눈날림(6), 눈날림(7) 
	final private Float R06;	// 6시간 강수량 | mm
	final private float REH;	// 습도 | %
	final private Float S06;	// 6시간 신적설 | cm
	final private float SKY;	// 하늘상태 | 맑음(1), 구름많음(3), 흐림(4) 
	final private float T3H;	// 3시간 기온 | 섭씨
	final private Float TMN;	// 아침 최저기온 | 섭씨
	final private Float TMX;	// 낮 최고기온 | 섭씨
	final private float UUU;	// 동서 바람성분 | 동(+표기), 서(-표기)
	final private float VVV;	// 남북 바람성분 | 북(+표기), 남(-표기)
	final private Float WAV;	// 파고 | M
	final private float VEC;	// 풍향 | (풍향값 + 22.5 * 0.5) / 22.5) = 변환값(소수점 이하 버림)
	final private float WSD;	// 풍속 | m/s
}
