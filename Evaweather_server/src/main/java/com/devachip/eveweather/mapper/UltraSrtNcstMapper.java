package com.devachip.eveweather.mapper;

import lombok.Getter;

@Getter
public class UltraSrtNcstMapper {
	private String updateSQL = "UPDATE UltraSrtNcsts SET T1H=?, RN1=?, UUU=?, VVV=?, REH=?, PTY=?, VEC=?, WSD=? "
			+ "WHERE baseDate=? AND baseTime=? AND nx=? AND ny=?";
	
	private String insertSQL = "INSERT INTO UltraSrtNcsts(baseDate, baseTime, nx, ny, T1H, RN1, UUU, VVV, REH, PTY, VEC, WSD) "
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private String deleteSQL = "DELETE FROM UltraSrtNcsts WHERE baseDate<?";
}
