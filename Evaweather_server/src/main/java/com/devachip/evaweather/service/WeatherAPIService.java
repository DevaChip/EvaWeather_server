package com.devachip.evaweather.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

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
	/** 동네예보 조회서비스 끝 */
}
