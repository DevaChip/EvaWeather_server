package com.devachip.eveweather.mapper;

import lombok.Getter;

@Getter
public class NowWeatherMapper {
	private String selectData = 
			"SELECT "
			+ "IFNULL(usn.T1H, usf.T1H) AS currentTemperature, IFNULL(usn.PTY, usf.PTY) AS pty, IFNULL(usn.RN1, usf.RN1) AS rn1, usf.LGT AS lgt, "
			+ "IFNULL(usn.REH, usf.REH) AS sd, IFNULL(usn.WSD, usf.WSD) AS windSpeed, IFNULL(usn.VEC, usf.VEC) AS windVector, "
			+ "usf.SKY AS sky, ttp.TMN AS minTemperature, ttp.TMX AS maxTemperatrue, ttp.POP AS pop "
			+ "FROM UltraSrtFcsts usf "
			+ "LEFT JOIN UltraSrtNcsts usn "
			+ "ON usf.fcstDate = usn.baseDate "
			+ "AND usf.fcstTime = usn.baseTime "
			+ "LEFT JOIN ("
			+ "	SELECT TMN, TMX, POP, tmn.fcstDate AS fcstDate, tmn.nx AS nx, tmn.ny AS ny "
			+ "	FROM (SELECT TMN, fcstDate , nx, ny FROM vilagefcsts WHERE fcstTime = '0600') tmn "
			+ "	LEFT JOIN (SELECT TMX, fcstDate, fcstTime, nx, ny FROM vilagefcsts WHERE fcstTime = '1500') tmx "
			+ "		ON tmn.fcstDate = tmx.fcstDate "
			+ "		AND tmn.nx = tmx.nx "
			+ "		AND tmn.ny = tmx.ny "
			+ "	LEFT JOIN (SELECT POP, fcstDate, fcstTime, nx, ny FROM vilagefcsts WHERE fcstTime = ?) pop "
			+ "		ON tmn.fcstDate = pop.fcstDate "
			+ "		AND tmn.nx = pop.nx "
			+ "		AND tmn.ny = pop.ny) ttp "
			+ "ON usf.fcstDate = ttp.fcstDate "
			+ "AND usf.nx = ttp.nx "
			+ "AND usf.ny = ttp.ny "
			+ "WHERE usf.fcstDate = ? AND usf.fcstTime = ? AND usf.nx = ? AND usf.ny = ?";
	
	private String selectTime = "SELECT fcstDate, fcstTime, T3H "
			+ "FROM VilageFcsts "
			+ "WHERE fcstDate "
			+ "	BETWEEN date_format(date_add(?, INTERVAL -1 DAY), '%Y%m%d') "
			+ "	AND	date_format(date_add(?, INTERVAL 1 DAY), '%Y%m%d') "
			+ "	AND nx = ? AND ny = ?";
}
