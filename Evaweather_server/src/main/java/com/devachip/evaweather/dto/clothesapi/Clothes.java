package com.devachip.evaweather.dto.clothesapi;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Clothes {
	private byte[] img;		// 옷 이미지
	private String link;	// 옷 정보 주소
}
