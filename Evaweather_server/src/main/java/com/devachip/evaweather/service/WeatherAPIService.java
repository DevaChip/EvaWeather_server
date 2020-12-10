package com.devachip.evaweather.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;

import com.devachip.evaweather.model.DataBean;
import com.devachip.evaweather.model.UltraSrtNcstRequest;
import com.devachip.evaweather.vo.AreaLocation;

/**
 * 날씨 API 받아오기
 * 
 * @author idean
 * @since 2020.11.26
 */
@Service
public class WeatherAPIService {

	/** 동네예보 조회서비스 */
	// 초단기 실황 조회
	public String getUltraSrtNcst(UltraSrtNcstRequest request) {
		// 필요한 객체 선언 및 초기화(추후 전역으로 바꿀지 확인)
		StringBuffer sb = new StringBuffer();

		try {
			// URL 설정
			/* 필수 */
			sb.append("http://apis.data.go.kr/1360000/VilageFcstInfoService/getUltraSrtNcst"); /* URL */
			sb.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + request.getServiceKey()); /* Service Key */
			sb.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "="
					+ URLEncoder.encode(request.getPageNo(), "UTF-8")); /* 페이지번호 */
			sb.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "="
					+ URLEncoder.encode(request.getNumOfRows(), "UTF-8")); /* 한 페이지 결과 수 */
			sb.append("&" + URLEncoder.encode("base_date", "UTF-8") + "="
					+ URLEncoder.encode(request.getBaseDate(), "UTF-8")); /* 발표 날짜(yyyyMMdd) */
			sb.append("&" + URLEncoder.encode("base_time", "UTF-8") + "="
					+ URLEncoder.encode(request.getBaseTime(), "UTF-8")); /* 발표 시각(hhmm) */

			/* 옵션 */
			sb.append("&" + URLEncoder.encode("dataType", "UTF-8") + "="
					+ URLEncoder.encode(request.getDataType(), "UTF-8")); /* 요청자료형식(XML/JSON)Default: XML */
			sb.append("&" + URLEncoder.encode("nx", "UTF-8") + "="
					+ URLEncoder.encode(Integer.toString(request.getNx()), "UTF-8")); /* 예보지점의 X 좌표값 */
			sb.append("&" + URLEncoder.encode("ny", "UTF-8") + "="
					+ URLEncoder.encode(Integer.toString(request.getNy()), "UTF-8")); /* 예보지점 Y 좌표 */

			URL url = new URL(sb.toString()); // url 세팅

			// Request 형식 설정
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000); // 연결시간 timeOut
			conn.setReadTimeout(5000); // InputStream 읽어오는 timeOut
			conn.setRequestProperty("Content-type", "application/json"); // response data 타입 설정

			conn.setRequestMethod("GET");
			conn.setDoOutput(true);

			int resCode = conn.getResponseCode();
			BufferedReader br;
			if (HttpURLConnection.HTTP_OK <= resCode && resCode <= HttpURLConnection.HTTP_MULT_CHOICE) {
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			} else {
				br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			}

			sb.setLength(0); // 버퍼 초기화
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			// 전달할 에러 메시지 설정
			sb.setLength(0);
			sb.append(e.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			// 전달할 에러 메시지 설정
			sb.setLength(0);
			sb.append(e.toString());
		}

		return sb.toString();
	}

	// 초단기 실황 조회 입력값 검증
	public String nowWeatherValidation(Map<String, Object> reqMap) {
		Map<String, Object> errorMap = new HashMap<String, Object>();
		
		if (reqMap == null || reqMap.size()==0) {
			return "{\"Error\":\"Request Param is Null.\"}";
		}
		
		Set<String> keys = reqMap.keySet();
		for (String key:keys) {
			Object value = reqMap.get(key);
			
			switch(key) {
			case "areaCode": // 행정구역코드
				try {
					// 빈 값인 경우
					if (StringUtils.isBlank((String)value)) {
						errorMap.put(key, String.format("%s value is empty.", key));
					} else {
						AreaLocation areaLocation = DataBean.getAreaLocations().get(value);

						// 행정구역코드값이 잘못된 경우 
						if (areaLocation==null) {
							errorMap.put(key, String.format("Invalid AreaCode : %s", value));
						}
					}
				} catch(ClassCastException e) {
					e.fillInStackTrace();
					
					errorMap.put(key, String.format("Invalid Data format(required=String) : %s", value));
				}
			}
		}
		
		String result = "";
		
		// 입력값이 잘못된 경우
		if (!errorMap.isEmpty()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorMap);
			} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				result = "{\"Error\":\"ErrorMsg to Json Failed.\"}";
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = "{\"Error\":\"ErrorMsg to Json Failed.\"}";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = "{\"Error\":\"ErrorMsg to Json Failed.\"}";
			}
		}
		
		return result;
	}
	/** 동네예보 조회서비스 끝 */
}
