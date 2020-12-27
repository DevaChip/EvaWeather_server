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
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.devachip.evaweather.bean.DataBean;
import com.devachip.evaweather.dbconnect.DBConnect;
import com.devachip.evaweather.model.VilageFcst;
import com.devachip.evaweather.model.VilageFcstRequest;
import com.devachip.evaweather.vo.LocationInfo;

/**
 * 동네예보 조회 데이터 수집 스케쥴러
 */
public class JobVilageFcst extends QuartzJobBean{
private static final String SERVICE_KEY = "5U%2F51omK%2FH%2F1Qf3TZG9f0QkCSHP9fpI9cAWdjV3xScZ6Sj9QFn4WL7pe8YldzB%2BZjrD1fVBrbNTS2pMDj6siAw%3D%3D";
	
	private final int DB_FAILED = 0;
	private final int DB_INSERTED = 1;
	private final int DB_UPDATED = 2;
	
	private final int CONNECT_TIMEOUT = 5000;
	private final int READ_TIMEOUT = 5000;
	
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		String jobName = context.getJobDetail().getKey().getName();
		
		System.out.println(String.format("===================== [%s] START =====================", jobName));
		if (DBConnect.getConnection() != null) {
			getVilageFcst();
		} else {
			System.out.println("DB Connect Failed. Job Stop.");
		}
		System.out.println(String.format("===================== [%s] END =====================", jobName));
	}
	
	// 초단기실황 업데이트 | 추가
	public void getVilageFcst() {
		// 현재 시간
		DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
		DateFormat tFormat = new SimpleDateFormat("HHmm");

		Date d = new Date();
		
		String currentDate = dFormat.format(d);
		String currentTime = tFormat.format(d);
		
		String schedulerName = "vilageFcst_Scheduler";
		DateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String nowTime = timeFormat.format(d);
		System.out.println(String.format("[AreaCode: %s][Scheduler] %s Start", nowTime, schedulerName));
		
		Set<String> keys = DataBean.getLocationInfoMap().keySet();
		System.out.println("locationInfoMap keys : " + keys.size());
		
		int updatedRows = 0;
		int insertedRows = 0;
		int failedRows = 0;
		VilageFcstRequest request = new VilageFcstRequest();
		for (String key: keys) {
			LocationInfo locationInfo = DataBean.getLocationInfoMap().get(key);
					
			// 필수 입력값 설정
			request.setPageNo("1");
			request.setNumOfRows("9999");
			request.setBaseDate(currentDate);
			request.setBaseTime(currentTime);

			// 옵션값 입력 설정
			request.setDataType("JSON");
			request.setNx(locationInfo.getNx());
			request.setNy(locationInfo.getNy());

			// API와 통신
			String apiName = "getVilageFcst";
			String getResult = getVilageFcstData(apiName, request);
			
			if (getResult==null) {	// API 통신에 실패한 경우
				System.out.println(String.format("[AreaCode: %s] Failed to receive response from server. Update Skip.", key));
				failedRows++;
				continue;
			}
			
			// API로부터 받은 데이터 파싱
			Map<String, Object> resultMap = jsonToObject(getResult);
			
			// 에러 코드 분류
			String resultCode = (String) resultMap.get("resultCode");
			if (StringUtils.equals(resultCode, "03")) {
				System.out.println("No data has been generated for the current time. Job Stop.");
				break;
			} else if (!StringUtils.equals(resultCode, "00")) {
				System.out.println(String.format("[AreaCode: %s] %s. Update Skip.", key, (String) resultMap.get("resultMsg")));
				failedRows++;
				continue;
			}
				
			// 데이터 업데이트 | 삽입
			VilageFcst dto = (VilageFcst)resultMap.get("dto");
			int dbResultCode = updateData(dto);
			
			switch(dbResultCode) {
			case DB_UPDATED:
				updatedRows++;
				break;
			case DB_INSERTED:
				insertedRows++;
				break;
			case DB_FAILED:
			default:
				System.out.println(String.format("[AreaCode: %s] DB update Failed.", key));
			}
		}
		
		System.out.println(String.format("AllRows: %d, updatedRows: %d, insertedRows: %d, failedRows: %d",keys.size(), updatedRows, insertedRows, failedRows));
		
		Date afterD = new Date();
		nowTime = timeFormat.format(afterD);
		System.out.println(String.format("[AreaCode: %s][Scheduler] %s End", nowTime, schedulerName));
	}
	
	
	/**
	 * 초단기실황, 초단기예보, 동네예보 조회 API 통신
	 * 
	 * @param apiName
	 * @param request
	 * @return	[통신 성공: String | 실패: null]
	 */
	public String getVilageFcstData(String apiName, VilageFcstRequest request) {
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
			conn.setConnectTimeout(CONNECT_TIMEOUT); // 연결시간 timeOut
			conn.setReadTimeout(READ_TIMEOUT); // InputStream 읽어오는 timeOut
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
 			
 			return sb.toString();
		} catch (MalformedURLException e) {
			System.out.println(e.fillInStackTrace());
		} catch (IOException e) {
			System.out.println(e.fillInStackTrace());
		}
		
		return null;
	}
	
	/**
	 * JSON -> List
	 * API 로부터 받아온 데이터를 객체화
	 * 
	 * @return Map{resultCode: 결과코드, dto: 초단기실황 데이터 객체} 
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> jsonToObject(String jsonString) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			Map<String, Object> map = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
			
			map = (Map<String, Object>)map.get("response");
			
			// 데이터가 조회됐는지 확인
			Map<String, Object> header = (Map<String, Object>)map.get("header");
			String resultCode = (String)header.get("resultCode");
			resultMap.put("resultCode", resultCode);
			resultMap.put("resultMsg", (String)header.get("resultMsg"));
			if ( !StringUtils.equals("00", resultCode) ) {	
				return resultMap;
			}
			
			map = (Map<String, Object>)map.get("body");
			map = (Map<String, Object>)map.get("items");
			List<Map<String, Object>> items = (List<Map<String, Object>>)map.get("item");
			
			Map<String, Object> dtoMap = new HashMap<>();
			for (Map<String, Object> item: items) {
				dtoMap.put("fcstDate", item.get("fcstDate"));
				dtoMap.put("fcstTime", item.get("fcstTime"));
				dtoMap.put("nx", item.get("nx"));
				dtoMap.put("ny", item.get("ny"));
				
				dtoMap.put((String) item.get("category"), Float.parseFloat((String) item.get("fcstValue")));
			}
			
			// TODO: Unrecognized Field "PTY" 오류로 해당 코드 사용 안됨. 확인 필요.
//			VilageFcst dto = mapper.convertValue(dtoMap, VilageFcst.class);
			
			VilageFcst dto = new VilageFcst();
			dto.setFcstDate((String) dtoMap.get("fcstDate"));
			dto.setFcstTime((String) dtoMap.get("fcstTime"));
			dto.setNx((int) dtoMap.get("nx"));
			dto.setNy((int) dtoMap.get("ny"));
			
			dto.setPOP((float) dtoMap.get("POP"));
			dto.setPTY((float) dtoMap.get("PTY"));
			dto.setR06((float) dtoMap.get("R06"));
			dto.setREH((float) dtoMap.get("REH"));
			dto.setS06((float) dtoMap.get("S06"));
			dto.setSKY((float) dtoMap.get("SKY"));
			dto.setT3H((float) dtoMap.get("T3H"));
			dto.setTMN((float) dtoMap.get("TMN"));
			dto.setTMX((float) dtoMap.get("TMX"));
			dto.setUUU((float) dtoMap.get("UUU"));
			dto.setVVV((float) dtoMap.get("VVV"));
			dto.setWAV((float) dtoMap.get("WAV"));
			dto.setVEC((float) dtoMap.get("VEC"));
			dto.setWSD((float) dtoMap.get("WSD"));
			
			resultMap.put("dto", dto);
			return resultMap;
		} catch(IOException e) {
			System.out.println(e.fillInStackTrace());
			resultMap = new HashMap<>();
		} catch(Exception e) {
			System.out.println(e.fillInStackTrace());
			resultMap = new HashMap<>();
		}
		
		return resultMap;
	}
	
	/**
	 * API 데이터 갱신 | 삽입
	 * 
	 * @param dto
	 * @return DB 작업 코드값 {0:실패, 1:삽입, 2: 갱신}
	 */
	@SuppressWarnings("resource")
	public synchronized int updateData(VilageFcst dto) {
		if (dto==null) {
			return DB_FAILED;
		}
		
		String updateSQL = "UPDATE VilageFcsts SET POP=?, PTY=?, R06=?, REH=?, S06=?, SKY=?, T3H=?, TMN=?, TMX=?, UUU=?, VVV=?, WAV=?, VEC=?, WSD=? "
				+ "WHERE fcstDate=? AND fcstTime=? AND nx=? AND ny=?";
		
		String insertSQL = "INSERT INTO VilageFcsts(fcstDate, fcstTime, nx, ny, POP, PTY, R06, REH, S06, SKY, T3H, TMN, TMX, UUU, VVV, WAV, VEC, WSD) "
				+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		PreparedStatement psmt = null;
		int updatedRow = 0;
		try {
			// 업데이트
			psmt = DBConnect.getConnection().prepareStatement(updateSQL);
			psmt.setFloat(1, dto.getPOP());
			psmt.setFloat(2, dto.getPTY());
			psmt.setFloat(3, dto.getR06());
			psmt.setFloat(4, dto.getREH());
			psmt.setFloat(5, dto.getS06());
			psmt.setFloat(6, dto.getSKY());
			psmt.setFloat(7, dto.getT3H());
			psmt.setFloat(8, dto.getTMN());
			psmt.setFloat(9, dto.getTMX());
			psmt.setFloat(10, dto.getUUU());
			psmt.setFloat(11, dto.getVVV());
			psmt.setFloat(12, dto.getWAV());
			psmt.setFloat(13, dto.getVEC());
			psmt.setFloat(14, dto.getWSD());
			psmt.setString(15, dto.getFcstDate());
			psmt.setString(16, dto.getFcstTime());
			psmt.setInt(17, dto.getNx());
			psmt.setInt(18, dto.getNy());
			
			updatedRow = psmt.executeUpdate();
			if (updatedRow==1) {
				return DB_UPDATED;
			}				
			
			// 수정할 데이터가 없을 경우 추가
			psmt = DBConnect.getConnection().prepareStatement(insertSQL);
			psmt.setString(1, dto.getFcstDate());
			psmt.setString(2, dto.getFcstTime());
			psmt.setInt(3, dto.getNx());
			psmt.setInt(4, dto.getNy());
			
			psmt.setFloat(5, dto.getPOP());
			psmt.setFloat(6, dto.getPTY());
			psmt.setFloat(7, dto.getR06());
			psmt.setFloat(8, dto.getREH());
			psmt.setFloat(9, dto.getS06());
			psmt.setFloat(10, dto.getSKY());
			psmt.setFloat(11, dto.getT3H());
			psmt.setFloat(12, dto.getTMN());
			psmt.setFloat(13, dto.getTMX());
			psmt.setFloat(14, dto.getUUU());
			psmt.setFloat(15, dto.getVVV());
			psmt.setFloat(16, dto.getWAV());
			psmt.setFloat(17, dto.getVEC());
			psmt.setFloat(18, dto.getWSD());
			
			updatedRow = psmt.executeUpdate();
			if (updatedRow==1) {
				return DB_INSERTED;
			}
		} catch (SQLException e){
			System.out.println(e.fillInStackTrace());
		} finally {
			// try 구문에서 중간에 return할 경우 리턴된 후 finally 코드가 실행된다.
			DBConnect.close(psmt);	
		}
		
		return DB_FAILED;
	}
}
