package com.devachip.evaweather.domain;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VilageFcst {
	private String tableName = "VilageFcsts";
	
	@NonNull private String fcstDate;
	@NonNull private String fcstTime;
	@NonNull private int nx;
	@NonNull private int ny;
	
	@NonNull private float POP;	// 강수확률 | %
	@NonNull private float PTY;	// 강수형태 | 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4), 빗방울(5), 빗방울/눈날림(6), 눈날림(7) 
	@NonNull private Float R06;	// 6시간 강수량 | mm
	@NonNull private float REH;	// 습도 | %
	@NonNull private Float S06;	// 6시간 신적설 | cm
	@NonNull private float SKY;	// 하늘상태 | 맑음(1), 구름많음(3), 흐림(4) 
	@NonNull private float T3H;	// 3시간 기온 | 섭씨
	@NonNull private Float TMN;	// 아침 최저기온 | 섭씨
	@NonNull private Float TMX;	// 낮 최고기온 | 섭씨
	@NonNull private float UUU;	// 동서 바람성분 | 동(+표기), 서(-표기)
	@NonNull private float VVV;	// 남북 바람성분 | 북(+표기), 남(-표기)
	@NonNull private Float WAV;	// 파고 | M
	@NonNull private float VEC;	// 풍향 | (풍향값 + 22.5 * 0.5) / 22.5) = 변환값(소수점 이하 버림)
	@NonNull private float WSD;	// 풍속 | m/s
}
