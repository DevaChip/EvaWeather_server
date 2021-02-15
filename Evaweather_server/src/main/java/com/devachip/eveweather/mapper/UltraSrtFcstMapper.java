package com.devachip.eveweather.mapper;

import com.devachip.evaweather.domain.UltraSrtFcst;

import lombok.Getter;

@Getter
public class UltraSrtFcstMapper {
	private String updateSQL = "UPDATE " + UltraSrtFcst.TABLE_NAME + " SET T1H=?, RN1=?, SKY=?, UUU=?, VVV=?, REH=?, PTY=?, LGT=?, VEC=?, WSD=? "
			+ "WHERE fcstDate=? AND fcstTime=? AND nx=? AND ny=?";
	
	private String insertSQL = "INSERT INTO " + UltraSrtFcst.TABLE_NAME + "(fcstDate, fcstTime, nx, ny, T1H, RN1, SKY, UUU, VVV, REH, PTY, LGT, VEC, WSD) "
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private String deleteSQL = "DELETE FROM " + UltraSrtFcst.TABLE_NAME + " WHERE fcstDate<?";
}
