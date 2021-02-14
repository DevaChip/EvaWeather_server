package com.devachip.evaweather.dto.clothesapi;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClothesResponse {
	private List<Clothes> clothes;	// 옷 정보
}
