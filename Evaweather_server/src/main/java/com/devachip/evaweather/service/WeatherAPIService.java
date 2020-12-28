package com.devachip.evaweather.service;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;

import com.devachip.evaweather.bean.DataBean;
import com.devachip.evaweather.dbconnect.DBConnect;
import com.devachip.evaweather.model.NowWeather;
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
	public String getNowWeather(String areaCode, String time) {
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
		
		
		StringBuffer sb = new StringBuffer();

		String selectSQL = "select ifnull(usn.T1H, usf.T1H) as currentTemperature, ifnull(usn.PTY, usf.PTY) as pty, ifnull(usn.RN1, usf.RN1) as rn1, ifnull(usn.REH, usf.REH)  as sd, "
				+ "	   ifnull(usn.WSD, usf.WSD) as windSpeed, ifnull(usn.VEC, usf.VEC) as windVector, usf.SKY as sky "
				+ "from ultrasrtfcsts usf "
				+ "left join ultrasrtncsts usn on usf.fcstDate = usn.baseDate and usf.fcstTime = usn.baseTime "
				+ "where usf.fcstDate=? and usf.fcstTime=? and usf.nx=? and usf.ny=?";
		
		PreparedStatement psmt = null;
		ResultSet rs = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			psmt = DBConnect.getConnection().prepareStatement(selectSQL);
			psmt.setString(1, request.getBaseDate());
			psmt.setString(2, request.getBaseTime());
			psmt.setInt(3, Integer.parseInt(request.getNx()));
			psmt.setInt(4, Integer.parseInt(request.getNy()));
			
			rs = psmt.executeQuery();

			// 결과값 세팅
			float currentTemperature = 0;
			float pty = 0;
			float rn1 = 0;
			float sd = 0;
			float windSpeed = 0;
			String windVector = "";
			float sky = 0;
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
			}
			
			String locationName = StringUtils.join(
					new String[] { locationInfo.getFirstArea(), locationInfo.getSecondArea(), locationInfo.getThirdArea() }, " ")
					.trim();
			
			NowWeather dto = new NowWeather();
			
			dto.setAreaCode(areaCode);
			dto.setLocationName(locationName);

			// 초단기실황 | 초단기예보
			dto.setCurrentTemperature(currentTemperature);
			dto.setPty(pty);
			dto.setRn1(rn1);
			dto.setSd(sd);
			dto.setWindSpeed(windSpeed);
			dto.setWindVector(windVector);
			
			// 초단기예보
			dto.setSky(sky);
			
			String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
			sb.append(jsonString);
		} catch (SQLException e) {
			System.out.println(e.fillInStackTrace());
		} catch (JsonGenerationException e) {
			System.out.println(e.fillInStackTrace());
		} catch (JsonMappingException e) {
			System.out.println(e.fillInStackTrace());
		} catch (IOException e) {
			System.out.println(e.fillInStackTrace());
		} finally {
			DBConnect.close(rs);
			DBConnect.close(psmt);
		}
		
		return sb.toString();
	}
}
