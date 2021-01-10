package com.devachip.eveweather.mapper;

import lombok.Getter;

@Getter
public class UltraSrtFcstMapper {
	private String updateSQL = "UPDATE UltraSrtFcsts SET T1H=?, RN1=?, SKY=?, UUU=?, VVV=?, REH=?, PTY=?, LGT=?, VEC=?, WSD=? "
			+ "WHERE fcstDate=? AND fcstTime=? AND nx=? AND ny=?";
	
	private String insertSQL = "INSERT INTO UltraSrtFcsts(fcstDate, fcstTime, nx, ny, T1H, RN1, SKY, UUU, VVV, REH, PTY, LGT, VEC, WSD) "
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
}
