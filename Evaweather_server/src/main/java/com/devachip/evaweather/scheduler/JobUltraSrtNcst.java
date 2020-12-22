package com.devachip.evaweather.scheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.devachip.evaweather.dbconnect.DBConnect;
import com.devachip.evaweather.model.DataBean;
import com.devachip.evaweather.model.UltraSrtNcst;
import com.devachip.evaweather.model.VilageFcstRequest;
import com.devachip.evaweather.vo.LocationInfo;

/**
 * API 데이터수집 스케쥴러
 */
@PersistJobDataAfterExecution
public class JobUltraSrtNcst extends QuartzJobBean {
	private static final String SERVICE_KEY = "5U%2F51omK%2FH%2F1Qf3TZG9f0QkCSHP9fpI9cAWdjV3xScZ6Sj9QFn4WL7pe8YldzB%2BZjrD1fVBrbNTS2pMDj6siAw%3D%3D";
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		String jobName = context.getJobDetail().getKey().getName();
		
		System.out.println(String.format("===================== [%s] START =====================", jobName));
		if (DBConnect.getConnection() != null) {
			getUltraSrtNcsts();
		} else {
			System.out.println("DB Connect Failed. Job Stop.");
		}
		System.out.println(String.format("===================== [%s] END =====================", jobName));
	}
	
	// 초단기실황 업데이트 | 추가
	public void getUltraSrtNcsts() {
		// 현재 시간
		DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
		DateFormat tFormat = new SimpleDateFormat("HHmm");
		Date d = new Date();
		String currentDate = dFormat.format(d); 
		String currentTime = tFormat.format(d);
		
		String schedulerName = "ultraSrtNcst_Scheduler";
		DateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String nowTime = timeFormat.format(d);
		System.out.println(String.format("[%s][Scheduler] %s Start", nowTime, schedulerName));
		
		Set<String> keys = DataBean.getLocationInfoMap().keySet();
		System.out.println("locationInfoMap keys : " + keys.size());
		int updatedRows = 0;
		for (String key: keys) {
			LocationInfo locationInfo = DataBean.getLocationInfoMap().get(key);
					
			// 필수 입력값 설정
			VilageFcstRequest request = new VilageFcstRequest();
			request.setPageNo("1");
			request.setNumOfRows("9999");
			request.setBaseDate(currentDate);
			request.setBaseTime(currentTime);

			// 옵션값 입력 설정
			request.setDataType("JSON");
			request.setNx(locationInfo.getNx());
			request.setNy(locationInfo.getNy());

			// API와 통신
			StringBuffer sb = new StringBuffer();
			String apiName = "getUltraSrtNcst";
			
			String getResult = getVilageFcst(apiName, request);
			sb.append(getResult);
			
			// API로부터 받은 데이터 파싱
			Map<String, Object> resultMap = jsonToObject(sb.toString());
			
			// 데이터가 조회되지 않은 경우
			if (!StringUtils.equals((String)resultMap.get("resultCode"), "00")) {
				 System.out.println(String.format("[%s] No data was retrieved. Update Skip.", key));
				continue;
			}
				
			// 데이터 업데이트 | 삽입
			UltraSrtNcst dto = (UltraSrtNcst)resultMap.get("dto");
			boolean isUpdated = updateData(dto);
			
			if (isUpdated) {
				updatedRows++;
			} else {		
				System.out.println(String.format("[%s] update Failed.", key));
			}
		}
		
		System.out.println("updatedRows : " + updatedRows);
		
		Date afterD = new Date();
		nowTime = timeFormat.format(afterD);
		System.out.println(String.format("[%s][Scheduler] %s End", nowTime, schedulerName));
	}
	
	/** 동네예보 조회서비스 */
	// 초단기실황, 초단기예보, 동네예보조회
	public String getVilageFcst(String apiName, VilageFcstRequest request) {
		StringBuffer sb = new StringBuffer();

		try {
			// URL 설정
			/* 필수 */
			sb.append("http://apis.data.go.kr/1360000/VilageFcstInfoService/" + apiName); /* URL */
			sb.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + SERVICE_KEY); /* Service Key */
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
	
	/**
	 * JSON -> List
	 * API 로부터 받아온 데이터를 객체화
	 * 
	 * @return Map{resultCode: 결과코드, dto: 초단기실황 데이터 객체} 
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> jsonToObject(String jsonString) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			Map<String, Object> map = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
			
			map = (Map<String, Object>)map.get("response");
			
			// 데이터가 조회됐는지 확인
			Map<String, Object> header = (Map<String, Object>)map.get("header");
			String resultCode = (String)header.get("resultCode");
			resultMap.put("resultCode", resultCode);
			if ( !StringUtils.equals("00", resultCode) ) {	
				return resultMap;
			}
			
			map = (Map<String, Object>)map.get("body");
			map = (Map<String, Object>)map.get("items");
			List<Map<String, Object>> items = (List<Map<String, Object>>)map.get("item");
			
			Map<String, Object> dtoMap = new HashMap<>();
			for (Map<String, Object> item: items) {
				dtoMap.put("baseDate", item.get("baseDate"));
				dtoMap.put("baseTime", item.get("baseTime"));
				dtoMap.put("nx", item.get("nx"));
				dtoMap.put("ny", item.get("ny"));
				
				dtoMap.put((String) item.get("category"), item.get("obsrValue"));
			}
			
			UltraSrtNcst dto = mapper.convertValue(dtoMap, UltraSrtNcst.class);
			resultMap.put("dto", dto);
			return resultMap;
		} catch(IOException e) {
			System.out.println(e.fillInStackTrace());
		} catch(Exception e) {
			System.out.println(e.fillInStackTrace());
		}
		
		return null;
	}
	
	@SuppressWarnings("resource")
	public synchronized boolean updateData(UltraSrtNcst dto) {
		if (dto==null) {
			return false;
		}
		
		String updateSQL = "UPDATE UltraSrtNcsts SET (T1H, RN1, UUU, VVV, REH, PTY, VEC, WSD) = (?, ?, ?, ?, ?, ?, ?, ?) "
				+ "WHERE baseDate=? AND baseTime=? AND nx=? AND ny=?";
		
		String insertSQL = "INSERT INTO UltraSrtNcsts(baseDate, baseTime, nx, ny, T1H, RN1, UUU, VVV, REH, PTY, VEC, WSD) "
				+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		PreparedStatement psmt = null;
		int updatedRow = 0;
		try {
			// 업데이트
			psmt = DBConnect.getConnection().prepareStatement(updateSQL);
			psmt.setString(1, dto.getT1H());
			psmt.setString(2, dto.getRN1());
			psmt.setString(3, dto.getUUU());
			psmt.setString(4, dto.getVVV());
			psmt.setString(5, dto.getREH());
			psmt.setString(6, dto.getPTY());
			psmt.setString(7, dto.getVEC());
			psmt.setString(8, dto.getWSD());
			
			psmt.setString(9, dto.getBaseDate());
			psmt.setString(10, dto.getBaseTime());
			psmt.setInt(11, dto.getNx());
			psmt.setInt(12, dto.getNy());			
			
			updatedRow = psmt.executeUpdate();
			if (updatedRow==1) {
				return true;
			}				
			
			// 수정할 데이터가 없을 경우 추가
			psmt = DBConnect.getConnection().prepareStatement(insertSQL);
			psmt.setString(1, dto.getBaseDate());
			psmt.setString(2, dto.getBaseTime());
			psmt.setInt(3, dto.getNx());
			psmt.setInt(4, dto.getNy());
			
			psmt.setString(5, dto.getT1H());
			psmt.setString(6, dto.getRN1());
			psmt.setString(7, dto.getUUU());
			psmt.setString(8, dto.getVVV());
			psmt.setString(9, dto.getREH());
			psmt.setString(10, dto.getPTY());
			psmt.setString(11, dto.getVEC());
			psmt.setString(12, dto.getWSD());
			
			updatedRow = psmt.executeUpdate();
			if (updatedRow==1) {
				return true;
			}
		} catch (SQLException e){
			System.out.println(e.fillInStackTrace());
		} finally {
			// try 구문에서 중간에 return할 경우 리턴된 후 finally 코드가 실행된다.
			DBConnect.close(psmt);	
		}
		
		return false;
	}
	/** 동네예보 조회서비스 끝 */
}
