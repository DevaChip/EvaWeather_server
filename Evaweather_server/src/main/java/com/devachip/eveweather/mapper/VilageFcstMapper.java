package com.devachip.eveweather.mapper;

import com.devachip.evaweather.domain.VilageFcst;

import lombok.Getter;

@Getter
public class VilageFcstMapper {
	private String updateSQL = "UPDATE " + VilageFcst.tableName + " SET POP=?, PTY=?, R06=?, REH=?, S06=?, SKY=?, T3H=?, TMN=?, TMX=?, UUU=?, VVV=?, WAV=?, VEC=?, WSD=? "
			+ "WHERE fcstDate=? AND fcstTime=? AND nx=? AND ny=?";
	
	private String insertSQL = "INSERT INTO " + VilageFcst.tableName + "(fcstDate, fcstTime, nx, ny, POP, PTY, R06, REH, S06, SKY, T3H, TMN, TMX, UUU, VVV, WAV, VEC, WSD) "
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private String deleteSQL = "DELETE FROM " + VilageFcst.tableName + " WHERE fcstDate<?";
}
