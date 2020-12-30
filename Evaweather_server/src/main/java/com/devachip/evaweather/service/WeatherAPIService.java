package com.devachip.evaweather.service;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;

import com.devachip.evaweather.bean.DataBean;
import com.devachip.evaweather.dbconnect.DBConnect;
import com.devachip.evaweather.model.NowWeather;
import com.devachip.evaweather.model.NowWeather_AirCondition;
import com.devachip.evaweather.model.NowWeather_Detail;
import com.devachip.evaweather.model.NowWeather_Time;
import com.devachip.evaweather.model.VilageFcstRequest;
import com.devachip.evaweather.vo.LocationInfo;

/**
 * 날씨 API 받아오기
 * 
 * @author idean
 * @since 2020.11.26
 */
@Service
public class WeatherAPIService {
	private final int YESTERDAY = 0;
	private final int TODAY = 1;
	private final int TOMORROW = 2;
	
	public String getNowWeather(String areaCode, String date, String time) {
		// 현재 시간
		DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
		DateFormat tFormat = new SimpleDateFormat("HHmm");
		Date d = new Date();
		String currentDate = dFormat.format(d); 
		String currentTime = tFormat.format(d);
		
		// TODO: 테스트용 코드, 최종본에선 삭제할 것
		if (StringUtils.isNotBlank(time)) {
			currentTime = time;
		}
		
		if (StringUtils.isNotBlank(date)) {
			currentDate = date;
		}
		
		// 현재시간 기준 가장 최근 정시를 기준으로 조회 
		String timePtn = "([0-1]{1}[0-9]{1}|2[0-3]{1})00";
		if (!currentTime.matches(timePtn)) {
			currentTime = StringUtils.substring(currentTime, 0,2) + "00";
		}
		
		// 행정구역코드에 맞는 좌표 정보
		LocationInfo locationInfo = DataBean.getLocationInfoMap().get(areaCode);
		
		VilageFcstRequest request = new VilageFcstRequest();
		request.setBaseDate(currentDate);
		request.setBaseTime(currentTime);
		request.setNx(locationInfo.getNx());
		request.setNy(locationInfo.getNy());
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			String locationName = StringUtils.join(
					new String[] { locationInfo.getFirstArea(), locationInfo.getSecondArea(), locationInfo.getThirdArea() }, " ")
					.trim();
			
			NowWeather dto = new NowWeather();
			
			dto.setAreaCode(areaCode);
			dto.setLocationName(locationName);

			// 데이터 세팅
			setData(dto, request);			
			
			// 어제, 오늘, 내일 시간대별 온도
			setTime(dto, request);
			
			// 모자란 부분 샘플로 채우기(테스트용)
			setSample(dto);
			
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
		} catch (JsonGenerationException e) {
			System.out.println(e.fillInStackTrace());
		} catch (JsonMappingException e) {
			System.out.println(e.fillInStackTrace());
		} catch (IOException e) {
			System.out.println(e.fillInStackTrace());
		} 
		
		return "{\"error\" : \"data parsing Error.\"}";
	}
	
	/**
	 * 데이터 조회 및 세팅
	 * 
	 * @param dto
	 * @param request
	 * @return boolean [true | false]
	 * 
	 * @author idean
	 * @since 2020.12.30
	 */
	private boolean setData(NowWeather dto, VilageFcstRequest request) {
		String selectSQL = 
				"SELECT "
				+ "IFNULL(usn.T1H, usf.T1H) AS currentTemperature, IFNULL(usn.PTY, usf.PTY) AS pty, IFNULL(usn.RN1, usf.RN1) AS rn1, "
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
		
		
		PreparedStatement psmt = null;
		ResultSet rs = null;
		
		try {
			psmt = DBConnect.getConnection().prepareStatement(selectSQL);
			
			/**
			 * 강수확률 기준 시간
			 * 
			 * 00시부터 3시간마다 강수확률이 존재하기 때문에 매 시간 강수확률을 표현하기가 애매함.
			 * 현재시간 포함 가장 최근의 강수확률을 보여주도록 함.
			 * 
			 * 예) 01시 -> 00시 강수확률 조회
			 *    03시 -> 03시 강수확률 조회
			 */
			String[] popTimes = {"2100", "1800", "1500", "1200", "0900", "0600", "0300", "0000"};
			String popTime = "";
			String currentTime = request.getBaseTime();
			
			for (String _time : popTimes) {
				if (StringUtils.compare(currentTime, _time) >= 0) {
					popTime = _time;
					break;
				}
			}
			psmt.setString(1, popTime);
			
			psmt.setString(2, request.getBaseDate());
			psmt.setString(3, request.getBaseTime());
			psmt.setInt(4, Integer.parseInt(request.getNx()));
			psmt.setInt(5, Integer.parseInt(request.getNy()));
			
			rs = psmt.executeQuery();

			// 결과값 세팅
			float currentTemperature = 0;
			float pty = 0;
			float rn1 = 0;
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
				sd = rs.getFloat(4);
				windSpeed = rs.getFloat(5);
				windVector = Optional.ofNullable(rs.getFloat(6))
				                 .map(x-> (int)((x+11.25)/22.5))
				                 .map(x -> VEC[x])
				                 .orElse("");
				sky = rs.getFloat(7);
				minTemperature = rs.getFloat(8);
				maxTemperature = rs.getFloat(9);
				pop = rs.getFloat(10);
			}
			
			// 초단기실황 | 초단기예보
			dto.setCurrentTemperature(currentTemperature);
			dto.setPty(pty);
			dto.setRn1(rn1);
			
			// 초단기예보
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
			
			return true;
		} catch (SQLException e) {
			
		} finally {
			DBConnect.close(rs);
			DBConnect.close(psmt);
		}
		
		return false;
	}
	
	/**
	 * 어제, 오늘, 내일 시간에 대한 온도 세팅
	 * 
	 * 어제, 오늘, 내일 날씨는 한줄로 출력하기 애매하여 따로 구하기로 함
	 * 
	 * @param dto
	 * @param request
	 * @return boolean [true | false]
	 * 
	 * @author idean
	 * @since 2020.12.30
	 */
	private boolean setTime(NowWeather dto, VilageFcstRequest request) {
		String selectTimeSQL = "SELECT fcstDate, fcstTime, T3H "
				+ "FROM VilageFcsts "
				+ "WHERE fcstDate "
				+ "	BETWEEN date_format(date_add(?, INTERVAL -1 DAY), '%Y%m%d') "
				+ "	AND	date_format(date_add(?, INTERVAL 1 DAY), '%Y%m%d') "
				+ "	AND nx = ? AND ny = ?";
		
		PreparedStatement psmt = null;
		ResultSet rs = null;
		
		try {
			// 시간 조회
			psmt = DBConnect.getConnection().prepareStatement(selectTimeSQL);
			psmt.setString(1, request.getBaseDate());
			psmt.setString(2, request.getBaseDate());
			psmt.setInt(3, Integer.parseInt(request.getNx()));
			psmt.setInt(4, Integer.parseInt(request.getNy()));
			
			rs = psmt.executeQuery();
			
			
			List<NowWeather_Time> timeList = new ArrayList<>(Arrays.asList(
					new NowWeather_Time[] { new NowWeather_Time(), new NowWeather_Time(), new NowWeather_Time() }));
			String currentDate = request.getBaseDate();
			while(rs.next()) {
				String fcstDate = rs.getString(1);
				String fcstTime = rs.getString(2);
				float T3H = rs.getFloat(3);
				
				int idx = 0;
				if (StringUtils.compare(fcstDate, currentDate)>0) {
					idx = TOMORROW;
				} else if (StringUtils.compare(fcstDate, currentDate)==0) {
					idx = TODAY;
				} else {
					idx = YESTERDAY;
				}
				
				NowWeather_Time time = timeList.get(idx);
				time.setDate(fcstDate);
				
				switch(fcstTime) {
				case "0000":
					time.setT0(T3H); break;
				case "0300":
					time.setT3(T3H); break;
				case "0600":
					time.setT6(T3H); break;
				case "0900":
					time.setT9(T3H); break;
				case "1200":
					time.setT12(T3H); break;
				case "1500":
					time.setT15(T3H); break;
				case "1800":
					time.setT18(T3H); break;
				case "2100":
					time.setT21(T3H); break;
				default:
					break;
				}
				
				timeList.set(idx, time);
			}
			dto.setTime(timeList);
			
			return true;
		} catch (SQLException e) {
			System.out.println(e.fillInStackTrace());
		} finally {
			DBConnect.close(rs);
			DBConnect.close(psmt);
		}
		
		return false;
	}
	
	private boolean setSample(NowWeather dto) {
		try {
			// 대기상태
			NowWeather_AirCondition airCondition = new NowWeather_AirCondition();
			airCondition.setPm10(30);
			airCondition.setPm25(60);
			airCondition.setPm10Grade(1);
			airCondition.setPm25Grade(1);
			airCondition.setO3((float) 0.043);
			airCondition.setO3Grade(1);
			
			dto.setAirCondition(airCondition);
			
			// 상세정보
			NowWeather_Detail detail = dto.getDetail();
			detail.setSunRise("074648");
			detail.setSunSet("172357");
			detail.setKhaiGrade(1);
			detail.setSo2Grade(1);
			detail.setCoGrade(1);
			detail.setNo2Grade(1);
			
			return true;
		} catch(Exception e) {
			System.out.println(e.fillInStackTrace());
		}
		
		return false;
	}
}
