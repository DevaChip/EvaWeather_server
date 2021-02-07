package com.devachip.eveweather.mapper;

import com.devachip.evaweather.domain.UltraSrtFcst;

import lombok.Getter;

@Getter
public class UltraSrtFcstMapper {
	private String updateSQL = "UPDATE " + UltraSrtFcst.tableName + " SET T1H=?, RN1=?, SKY=?, UUU=?, VVV=?, REH=?, PTY=?, LGT=?, VEC=?, WSD=? "
			+ "WHERE fcstDate=? AND fcstTime=? AND nx=? AND ny=?";
	
	private String insertSQL = "INSERT INTO " + UltraSrtFcst.tableName + "(fcstDate, fcstTime, nx, ny, T1H, RN1, SKY, UUU, VVV, REH, PTY, LGT, VEC, WSD) "
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private String deleteSQL = "DELETE FROM " + UltraSrtFcst.tableName + " WHERE fcstDate<?";
}
