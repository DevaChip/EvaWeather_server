package com.devachip.evaweather.model;

import lombok.Getter;

@Getter
public class UltraSrtNcst {
	private String baseDate;
	private String baseTime;
	private int nx;
	private int ny;
	private String category;
	private float obsrValue;
}
