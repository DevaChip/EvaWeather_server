package com.devachip.eveweather.mapper;

import com.devachip.evaweather.domain.UltraSrtNcst;

import lombok.Getter;

@Getter
public class UltraSrtNcstMapper {
	private String updateSQL = "UPDATE " + UltraSrtNcst.TABLE_NAME + " SET T1H=?, RN1=?, UUU=?, VVV=?, REH=?, PTY=?, VEC=?, WSD=? "
			+ "WHERE baseDate=? AND baseTime=? AND nx=? AND ny=?";
	
	private String insertSQL = "INSERT INTO " + UltraSrtNcst.TABLE_NAME + "(baseDate, baseTime, nx, ny, T1H, RN1, UUU, VVV, REH, PTY, VEC, WSD) "
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private String deleteSQL = "DELETE FROM " + UltraSrtNcst.TABLE_NAME + " WHERE baseDate<?";
}
