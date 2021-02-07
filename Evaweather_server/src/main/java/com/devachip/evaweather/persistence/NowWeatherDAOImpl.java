package com.devachip.evaweather.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.devachip.evaweather.bean.DBConnect;
import com.devachip.evaweather.dto.NowWeather;
import com.devachip.evaweather.dto.NowWeather_DayInfo;
import com.devachip.evaweather.dto.NowWeather_DayInfo_TimeWeather;
import com.devachip.evaweather.dto.NowWeather_Detail;
import com.devachip.evaweather.dto.VilageFcstRequest;
import com.devachip.eveweather.mapper.NowWeatherMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class NowWeatherDAOImpl implements NowWeatherDAO {
	
	private DBConnect dbConnect;
	private NowWeatherMapper mapper = new NowWeatherMapper();
	
	// getTime 관련 상수
	private final int YESTERDAY = 0;
	private final int TODAY = 1;
	private final int TOMORROW = 2;
	
	public NowWeatherDAOImpl(DBConnect dbConnect) {
		this.dbConnect = dbConnect; 
	}

	@Override
	public NowWeather getData(VilageFcstRequest request) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		ResultSet rs = null;
		
		try {
			NowWeather dto = new NowWeather();
			psmt = conn.prepareStatement(mapper.getSelectData());
			
			psmt.setString(1, request.getPopTime());
			psmt.setString(2, request.getBaseDate());
			psmt.setString(3, request.getBaseTime());
			psmt.setInt(4, Integer.parseInt(request.getNx()));
			psmt.setInt(5, Integer.parseInt(request.getNy()));
			
			rs = psmt.executeQuery();
			
			// 결과값 세팅
			float currentTemperature = 0;
			float pty = 0;
			float rn1 = 0;
			float lgt = 0;
			float sd = 0;
			float windSpeed = 0;
			String windVector = "";
			float sky = 0;
			float minTemperature = 0;
			float maxTemperature = 0;
			float pop = 0;
			String[] VEC = {"N", "NNE", "NE", "ENE", 
							"E", "ESE", "SE", "SSE", 
							"S", "SSW", "SW", "WSW", 
							"W", "WNW", "NW", "NNW", 
							"N"};
			
			while(rs.next()) {
				currentTemperature = rs.getFloat(1);
				pty = rs.getFloat(2);
				rn1 = rs.getFloat(3);
				lgt = rs.getFloat(4);
				sd = rs.getFloat(5);
				windSpeed = rs.getFloat(6);
				windVector = Optional.ofNullable(rs.getFloat(7))
				                 .map(x-> (int)((x+11.25)/22.5))
				                 .map(x -> VEC[x])
				                 .orElse("");
				sky = rs.getFloat(8);
				minTemperature = rs.getFloat(9);
				maxTemperature = rs.getFloat(10);
				pop = rs.getFloat(11);
			}
			
			// 초단기실황 | 초단기예보
			dto.setCurrentTemperature(currentTemperature);
			dto.setPty(pty);
			dto.setRn1(rn1);
			
			// 초단기예보
			dto.setLgt(lgt);
			dto.setSky(sky);
			
			// 동네예보
			dto.setMinTemperature(minTemperature);
			dto.setMaxTemperature(maxTemperature);
			dto.setPop(pop);
			
			// 상세정보
			NowWeather_Detail detail = new NowWeather_Detail();
			detail.setSd(sd);
			detail.setWindSpeed(windSpeed);
			detail.setWindVector(windVector);
			dto.setDetail(detail);
			
			return dto;
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			DBConnect.close(rs);
			DBConnect.close(psmt);
		}
		
		return null;
	}

	@Override
	public List<NowWeather_DayInfo> getDaysInfo(VilageFcstRequest request) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		ResultSet rs = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getSelectDayInfo());
			
			psmt.setString(1, request.getBaseDate());
			psmt.setString(2, request.getBaseDate());
			psmt.setInt(3, Integer.parseInt(request.getNx()));
			psmt.setInt(4, Integer.parseInt(request.getNy()));
			
			rs = psmt.executeQuery();
			
			List<NowWeather_DayInfo> dayInfoList = new ArrayList<>(Arrays.asList(
					new NowWeather_DayInfo[] { new NowWeather_DayInfo(), new NowWeather_DayInfo(), new NowWeather_DayInfo() }));
			String currentDate = request.getBaseDate();
			while(rs.next()) {
				String fcstDate = rs.getString(1);
				String fcstTime = rs.getString(2);
				float t3h = rs.getFloat(3);
				float pop = rs.getFloat(4);
				float sky = rs.getFloat(5);
				float pty = rs.getFloat(6);
				float rn1 = rs.getFloat(7);
				
				NowWeather_DayInfo_TimeWeather timeWeather = new NowWeather_DayInfo_TimeWeather(t3h, pop, sky, pty, rn1);
				
				int idx = 0;
				if (StringUtils.compare(fcstDate, currentDate)>0) {
					idx = TOMORROW;
				} else if (StringUtils.compare(fcstDate, currentDate)==0) {
					idx = TODAY;
				} else {
					idx = YESTERDAY;
				}
				
				NowWeather_DayInfo dayInfo = dayInfoList.get(idx);
				dayInfo.setDate(fcstDate);
				
				switch(fcstTime) {
				case "0000":
					dayInfo.setT0(timeWeather); break;
				case "0300":
					dayInfo.setT3(timeWeather); break;
				case "0600":
					dayInfo.setT6(timeWeather); break;
				case "0900":
					dayInfo.setT9(timeWeather); break;
				case "1200":
					dayInfo.setT12(timeWeather); break;
				case "1500":
					dayInfo.setT15(timeWeather); break;
				case "1800":
					dayInfo.setT18(timeWeather); break;
				case "2100":
					dayInfo.setT21(timeWeather); break;
				default:
					break;
				}
				
				dayInfoList.set(idx, dayInfo);
			}
			
			return dayInfoList;
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			DBConnect.close(rs);
			DBConnect.close(psmt);
		}
		
		return null;
	}

}
