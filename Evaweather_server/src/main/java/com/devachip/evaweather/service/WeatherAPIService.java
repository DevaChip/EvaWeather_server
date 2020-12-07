package com.devachip.evaweather.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;

import com.devachip.evaweather.model.UltraSrtNcstRequest;

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
					+ URLEncoder.encode(request.getNx(), "UTF-8")); /* 예보지점의 X 좌표값 */
			sb.append("&" + URLEncoder.encode("ny", "UTF-8") + "="
					+ URLEncoder.encode(request.getNy(), "UTF-8")); /* 예보지점 Y 좌표 */

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
	public String nowWeatherValidation(HttpServletRequest req) {
		Map<String, Object> errorMap = new HashMap<String, Object>();
		
		if (req == null) {
			return "{\"Error\":\"requestParam is Null.\"}";
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		DateFormat timeFormat = new SimpleDateFormat("hhmm");
		List<String> dataTypeList = Arrays.asList(new String[] {"JSON", "XML"});

		int requiredParams = 0;	// 필수 입력값 갯수 확인
		
		Enumeration<String> keys = req.getParameterNames(); 
 		while(keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			Object value = req.getParameter(key);
			
			// 빈 값인 경우
			if (value == null) {
				errorMap.put(key, "data is Null");
				continue;
			}
			
			switch (key) {
			/* 필수 입력값 */
			case "baseDate":
				try {
					Date d = dateFormat.parse((String) value);
					
					// 값이 제대로 된 경우
					if (StringUtils.equals((String)value, dateFormat.format(d))) {
						requiredParams++;	// 필수 입력값 카운트
					} 
					// 잘못된 값인 경우
					else {
						errorMap.put(key, "invalid data:" + (String)value);
					}
				} 
				// 데이터 포맷이 틀린 경우(yyyyMMdd)
				catch (ParseException e) {
					errorMap.put(key, "invalid data format(yyyyMMdd):" + (String)value);
				}
				// 데이터 자료형이 틀린 경우(String)
				catch (ClassCastException e) {
					errorMap.put(key, "invalid data type(String):" + (String)value);
				}
				break;
			case "baseTime":
				try {
					Date d = timeFormat.parse((String) value);
					
					// 값이 제대로 된 경우
					if (!StringUtils.equals((String)value, timeFormat.format(d))) {
						requiredParams++;	// 필수 입력값 카운트
					} 
					// 잘못된 값인 경우
					else {
						errorMap.put(key, "invalid data:" + (String)value);
					}
				} 
				// 데이터 포맷이 틀린 경우(hhmm)
				catch (ParseException e) {
					errorMap.put(key, "invalid data format(hhmm):" + (String)value);
				}
				// 데이터 자료형이 틀린 경우(String)
				catch (ClassCastException e) {
					errorMap.put(key, "invalid data type(String):" + (String)value);
				}
				break;
			case "pageNo":
				try {
					Integer pageNo = Integer.parseInt((String)value);
					
					// 값이 제대로 된 경우
					if (pageNo>0) {
						requiredParams++;	// 필수 입력값 카운트
					} 
					// 잘못된 값인 경우
					// TODO: numOfRows 보다 값이 큰 경우
					else {
						errorMap.put(key, "invalid data:" + (String)value);
					}
				} 
				// 데이터 포맷이 틀린 경우(Number)
				catch (NumberFormatException e) {
					errorMap.put(key, "invalid data format(Number):" + (String)value);
				}
				// 데이터 자료형이 틀린 경우(String)
				catch (ClassCastException e) {
					errorMap.put(key, "invalid data type(String):" + (String)value);
				}
				break;
			case "numOfRows":
				try {
					Integer numOfRows = Integer.parseInt((String)value);
					
					// 값이 제대로 된 경우
					if (numOfRows>0) {
						requiredParams++;	// 필수 입력값 카운트
					} 
					// 잘못된 값인 경우					
					else {
						errorMap.put(key, "invalid data:" + (String)value);
					}
				} 
				// 데이터 포맷이 틀린 경우(Number)
				catch (NumberFormatException e) {
					errorMap.put(key, "invalid data format(Number):" + (String)value);
				}
				// 데이터 자료형이 틀린 경우(String)
				catch (ClassCastException e) {
					errorMap.put(key, "invalid data type(String):" + (String)value);
				}
				break;

			/* 옵션 값 */
			case "dataType":
				try {
					// 잘못된 값인 경우
					if (!dataTypeList.contains((String)value)) {
						errorMap.put(key, "invalid data[JSON|XML]:" + (String)value);
					}
				}
				// 데이터 자료형이 틀린 경우(String)
				catch (ClassCastException e) {
					errorMap.put(key, "invalid data type(String):" + (String)value);
				}
				break;
			case "nx":
				try {
					Integer nx = Integer.parseInt((String)value);
					
					// 잘못된 값인 경우
					if (nx<=0) {
						errorMap.put(key, "invalid data:" + (String)value);
					}
				} 
				// 데이터 포맷이 틀린 경우(Number)
				catch (NumberFormatException e) {
					errorMap.put(key, "invalid data format(Number):" + (String)value);
				}
				// 데이터 자료형이 틀린 경우(String)
				catch (ClassCastException e) {
					errorMap.put(key, "invalid data type(String):" + (String)value);
				}
				break;
			case "ny":
				try {
					Integer ny = Integer.parseInt((String)value);
					
					// 잘못된 값인 경우
					if (ny<=0) {
						errorMap.put(key, "invalid data:" + (String)value);
					}
				} 
				// 데이터 포맷이 틀린 경우(Number)
				catch (NumberFormatException e) {
					errorMap.put(key, "invalid data format(Number):" + (String)value);
				}
				// 데이터 자료형이 틀린 경우(String)
				catch (ClassCastException e) {
					errorMap.put(key, "invalid data type(String):" + (String)value);
				}
				break;
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
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 필수 입력값이 입력되지 않은 경우
		else if (requiredParams<4) {
			result = "{\"Error\":\"required value not entered.\""
					+ ",\"requiredParams\":\"baseDate, baseTime, pageNo, numOfRows\"}";
		}
		
		return result;
	}
	/** 동네예보 조회서비스 끝 */
}
