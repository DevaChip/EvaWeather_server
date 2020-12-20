package com.devachip.evaweather.service;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;

import com.devachip.evaweather.dbconnect.DBConnect;
import com.devachip.evaweather.model.VilageFcstRequest;

/**
 * 날씨 API 받아오기
 * 
 * @author idean
 * @since 2020.11.26
 */
@Service
public class WeatherAPIService {
	/** 동네예보 조회서비스 */
	// 초단기실황
	public String getNowWeather(VilageFcstRequest request) {
		StringBuffer sb = new StringBuffer();

		String selectSQL = "SELECT category, obsrValue FROM UltraSrtNcsts "
				+ "WHERE baseDate=? AND baseTime=? AND nx=? AND ny=?";
		
		PreparedStatement psmt = null;
		ResultSet rs = null;
		try {
			psmt = DBConnect.getConnection().prepareStatement(selectSQL);
			psmt.setString(1, request.getBaseDate());
			psmt.setString(2, request.getBaseTime());
			psmt.setInt(3, Integer.parseInt(request.getNx()));
			psmt.setInt(4, Integer.parseInt(request.getNy()));
			
			rs = psmt.executeQuery();
			Map<String, Object> map = new HashMap<>();
			while(rs.next()) {
				String category = rs.getString(1);
				float obsrValue = rs.getFloat(2);
				
				map.put(category, obsrValue);
			}
			
			ObjectMapper mapper = new ObjectMapper();
			String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
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
	/** 동네예보 조회서비스 끝 */
}
