package com.devachip.evaweather.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NowWeather_Clothes {
	private byte[] img;		// 옷 이미지
	private String link;	// 옷 정보 주소
}
