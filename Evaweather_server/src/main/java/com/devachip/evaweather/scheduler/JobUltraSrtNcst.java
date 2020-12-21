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
import java.util.ArrayList;
import java.util.Date;
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
		String jobName = context.getJobDetail().getDescription();
		
		System.out.println(String.format("===================== [%s] START =====================", jobName));
		getUltraSrtNcsts();
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
			sb.append(getVilageFcst(apiName, request));
			
			// 데이터 업데이트 | 삽입
			List<UltraSrtNcst> dtoList = jsonToList(sb.toString());
			boolean isUpdated = updateData(dtoList);
			
			if (!isUpdated) {
				System.out.println(String.format("[%s] update Failed.", key));
			}			
		}
		
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
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public List<UltraSrtNcst> jsonToList(String jsonString) {
		ObjectMapper mapper = new ObjectMapper();
		List<UltraSrtNcst> list = new ArrayList<>();
		try {
			Map<String, Object> map = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
			
			map = (Map<String, Object>)map.get("response");
			
			// 데이터가 조회됬는지 확인
			Map<String, Object> header = (Map<String, Object>)map.get("header");
			if ( !StringUtils.equals("00", (String)header.get("resultCode")) ) {	
				return list;	// 조회되지 않은 경우 빈 리스트 리턴
			}
			
			map = (Map<String, Object>)map.get("body");
			map = (Map<String, Object>)map.get("items");
			List<Map<String, Object>> items = (List<Map<String, Object>>)map.get("item");
			
			for (Map<String, Object> item: items) {
				UltraSrtNcst dto = mapper.convertValue(item, UltraSrtNcst.class);
				
				list.add(dto);
			}
			
			return list;
		} catch(IOException e) {
			System.out.println(e.fillInStackTrace());
		} catch(Exception e) {
			System.out.println(e.fillInStackTrace());
		}
		
		return null;
	}
	
	public synchronized boolean updateData(List<UltraSrtNcst> dtoList) {
		if (dtoList==null || dtoList.size()==0) {
			return true;
		}
		
		String insertSQL = "INSERT INTO UltraSrtNcsts(baseDate, baseTime, nx, ny, category, obsrValue) "
				+ "VALUES(?, ?, ?, ?, ?, ?)";
		
		String updateSQL = "UPDATE UltraSrtNcsts SET obsrValue=? "
				+ "WHERE baseDate=? AND baseTime=? AND nx=? AND ny=? AND category=?";
		
		PreparedStatement psmt = null;
		int updatedCnt = 0;
		int updatedRow = 0;
		for (UltraSrtNcst dto: dtoList) {
			try {
				// 업데이트
				psmt = DBConnect.getConnection().prepareStatement(updateSQL);
				psmt.setFloat(1, dto.getObsrValue());
				psmt.setString(2, dto.getBaseDate());
				psmt.setString(3, dto.getBaseTime());
				psmt.setInt(4, dto.getNx());
				psmt.setInt(5, dto.getNy());
				psmt.setString(6, dto.getCategory());
				
				updatedRow = psmt.executeUpdate();
				if (updatedRow==1) {
					updatedCnt++;
					continue;
				}				
				
				// 수정할 데이터가 없을 경우 추가
				psmt = DBConnect.getConnection().prepareStatement(insertSQL);
				psmt.setString(1, dto.getBaseDate());
				psmt.setString(2, dto.getBaseTime());
				psmt.setInt(3, dto.getNx());
				psmt.setInt(4, dto.getNy());
				psmt.setString(5, dto.getCategory());
				psmt.setFloat(6, dto.getObsrValue());
				
				updatedRow = psmt.executeUpdate();
				updatedCnt += updatedRow;
			} catch (SQLException e){
				System.out.println(e.fillInStackTrace());
			}
		}
		
		DBConnect.close(psmt);
		
		// 리스트 전부 업데이트되었다면 true
		if (updatedCnt==dtoList.size()) {
			return true;
		} else {
			return false;
		}
	}
	/** 동네예보 조회서비스 끝 */
}
