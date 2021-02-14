package com.devachip.evaweather.scheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.devachip.evaweather.base.PropertiesConfig;
import com.devachip.evaweather.bean.DataBean;
import com.devachip.evaweather.domain.UltraSrtFcst;
import com.devachip.evaweather.dto.weatherapi.VilageFcstRequest;
import com.devachip.evaweather.persistence.UltraSrtFcstDAO;
import com.devachip.evaweather.persistence.UltraSrtFcstDAOImpl;
import com.devachip.evaweather.util.BeanUtils;
import com.devachip.evaweather.vo.LocationInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 초단기예보 데이터 수집 스케쥴러
 */
@Slf4j
public class JobUltraSrtFcst extends QuartzJobBean{
private static final String SERVICE_KEY = "5U%2F51omK%2FH%2F1Qf3TZG9f0QkCSHP9fpI9cAWdjV3xScZ6Sj9QFn4WL7pe8YldzB%2BZjrD1fVBrbNTS2pMDj6siAw%3D%3D";
	
	private final int DB_FAILED = 0;
	private final int DB_INSERTED = 1;
	private final int DB_UPDATED = 2;
	
	private final int CONNECT_TIMEOUT = 10000;
	private final int READ_TIMEOUT = 10000;
	
	private StringBuffer sb = new StringBuffer();
	private UltraSrtFcstDAO dao = (UltraSrtFcstDAO) BeanUtils.getBean(UltraSrtFcstDAOImpl.class);
	private DataBean dataBean = (DataBean) BeanUtils.getBean(DataBean.class);
	
	private PropertiesConfig properties = (PropertiesConfig) BeanUtils.getBean(PropertiesConfig.class);
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		String jobName = context.getJobDetail().getKey().getName();
		String jobDetail = context.getJobDetail().getKey().getName();
		
		log.debug("===================== [{}] START =====================", jobName);
		getUltraSrtFcsts(jobDetail);
		log.debug("\n" + sb.toString());	// 실행결과 한번에 출력
		log.debug("===================== [{}] END =====================", jobName);
	}
	
	// 초단기실황 업데이트 | 추가
	@SuppressWarnings("unchecked")
	public void getUltraSrtFcsts(String jobDetail) {
		// 현재 시간
		DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
		DateFormat tFormat = new SimpleDateFormat("HHmm");

		Date d = new Date();
		
		String currentDate = dFormat.format(d);
		String currentTime = tFormat.format(d);
		
		DateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String nowTime = timeFormat.format(d);
		sb.append(String.format("[%s][Scheduler] %s Start\n", nowTime, jobDetail));
		
		int updatedRows = 0;
		int insertedRows = 0;
		int failedRows = 0;
		VilageFcstRequest request = new VilageFcstRequest();
		for (LocationInfo info: dataBean.getLocationInfoList_schedule()) {
			// 필수 입력값 설정
			request.setPageNo("1");
			request.setNumOfRows("9999");
			request.setBaseDate(currentDate);
			request.setBaseTime(currentTime);

			// 옵션값 입력 설정
			request.setDataType("JSON");
			request.setNx(info.getNx());
			request.setNy(info.getNy());

			// API와 통신
			String apiName = "getUltraSrtFcst";
			String getResult = getVilageFcstData(apiName, request);
			
			if (getResult==null) {	// API 통신에 실패한 경우
				sb.append(String.format("(%s, %s) Failed to receive response from server. Update Skip.\n", info.getNx(), info.getNy()));
				failedRows++;
				continue;
			}
			
			// API로부터 받은 데이터 파싱
			Map<String, Object> resultMap = jsonToObject(getResult);
			
			// 에러 코드 분류
			String resultCode = (String) resultMap.get("resultCode");
			if (StringUtils.equals(resultCode, "03")) {
				sb.append("No data has been generated for the current time. Job Stop.\n");
				break;
			} else if (!StringUtils.equals(resultCode, "00")) {
				sb.append(String.format("(%s, %s) %s. Update Skip.\n", info.getNx(), info.getNy(), (String) resultMap.get("resultMsg")));
				failedRows++;
				continue;
			}
				
			// 데이터 업데이트 | 삽입
			List<UltraSrtFcst> entityList = (List<UltraSrtFcst>) resultMap.getOrDefault("entityList", new ArrayList<UltraSrtFcst>());
			for (UltraSrtFcst entity : entityList) {
				int dbResultCode = updateData(entity);
				
				switch(dbResultCode) {
				case DB_UPDATED:
					updatedRows++;
					break;
				case DB_INSERTED:
					insertedRows++;
					break;
				case DB_FAILED:
				default:
					sb.append(String.format("(%d, %d) %s-%s DB update Failed.\n", entity.getNx(), entity.getNy(), entity.getFcstDate(), entity.getFcstTime()));
				}
			}
		}
		
		sb.append(String.format("AllRows: %d, updatedRows: %d, insertedRows: %d, failedRows: %d\n",
				dataBean.getLocationInfoList_schedule().size(), updatedRows, insertedRows, failedRows));
		
		Date afterD = new Date();
		String afterTime = timeFormat.format(afterD);
		sb.append(String.format("[%s][Scheduler] %s End\n", afterTime, jobDetail));
		
		float runTime = (float) ((afterD.getTime() - d.getTime())/1000.0);
		sb.append(String.format("runTime: %dm %.3fs", (int) (runTime/60), runTime%60));
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

		long startTime = new Date().getTime();
		
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
				br = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
			} else {
				br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), Charset.forName("UTF-8")));
			}

			sb.setLength(0); // 버퍼 초기화
			String line = "";
 			while ((line = br.readLine()) != null) {
 				sb.append(line);
			}
 			
 			return sb.toString();
		} catch (MalformedURLException e) {
			sb.append(e.fillInStackTrace() + "\n");
		} catch (IOException e) {
			sb.append(e.fillInStackTrace() + "\n");
		} finally {
			if (properties.isDebugMode_scheduler()) {
				long endTime = new Date().getTime();
				float runTime = (float) ((endTime - startTime)/1000.0);
				log.debug("({}, {}) API runTime : {}s", request.getNx(), request.getNy(), runTime%60);
			}
		}
		
		return null;
	}
	
	/**
	 * JSON -> List
	 * API 로부터 받아온 데이터를 객체화
	 * 
	 * @return Map{resultCode: 결과코드, entity: 초단기실황 데이터 객체} 
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> jsonToObject(String jsonString) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		
		long startTime = new Date().getTime();
		try {
			Map<String, Object> map = mapper.reader().readValue(jsonString, Map.class);
			
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
			
			Map<Fcst_Key, Map<String, Object>> dtoMap = new HashMap<>();
			for (Map<String, Object> item: items) {
				Fcst_Key key = new Fcst_Key((String) item.get("fcstDate"),
						(String) item.get("fcstTime"), (int) item.get("nx"), (int) item.get("ny"));
				
				if (dtoMap.get(key)==null) {
					dtoMap.put(key, new HashMap<String, Object>());
				}
				
				Map<String, Object> category = dtoMap.get(key);
				category.put((String) item.get("category"), Float.parseFloat((String) item.get("fcstValue")));
				
				dtoMap.put(key, category);
			}
			
			Set<Fcst_Key> keys = dtoMap.keySet();
			List<UltraSrtFcst> entityList = new ArrayList<>();
			for (Fcst_Key key: keys) {
				Map<String, Object> category = dtoMap.get(key);
				
				UltraSrtFcst entity = new UltraSrtFcst(key.getFcstDate(), key.getFcstTime(), key.getNx(), key.getNy(),
						(float) category.get("T1H"), (float) category.get("RN1"), (float) category.get("SKY"), (float) category.get("UUU"),
						(float) category.get("VVV"), (float) category.get("REH"), (float) category.get("PTY"), (float) category.get("LGT"),
						(float) category.get("VEC"), (float) category.get("WSD"));
				entityList.add(entity);
			}
			
			resultMap.put("entityList", entityList);
			return resultMap;
		} catch(IOException e) {
			sb.append(e.fillInStackTrace() + "\n");
			resultMap = new HashMap<>();
		} catch(Exception e) {
			sb.append(e.fillInStackTrace() + "\n");
			resultMap = new HashMap<>();
		} finally {
			if (properties.isDebugMode_scheduler()) {
				long endTime = new Date().getTime();
				float runTime = (float) ((endTime - startTime)/1000.0);
				log.debug("jsonToObject runTime : {}s", runTime%60);
			}
		}
		
		return resultMap;
	}
	
	/**
	 * API 데이터 갱신 | 삽입
	 * 
	 * @param entity
	 * @return DB 작업 코드값 {0:실패, 1:삽입, 2: 갱신}
	 */
	public synchronized int updateData(UltraSrtFcst entity) {
		long startTime = new Date().getTime();
		
		try {
			if (entity == null) {
				return DB_FAILED;
			}
			
			if (dao.update(entity) == 1) {
				return DB_UPDATED;
			}
			
			if (dao.insert(entity) == 1) {
				return DB_INSERTED;
			}
		} finally {
			if (properties.isDebugMode_scheduler()) {
				long endTime = new Date().getTime();
				float runTime = (float) ((endTime - startTime)/1000.0);
				log.debug("({}, {}) updateData runTime : {}s", entity.getNx(), entity.getNy(), runTime%60);
			}
		}
		
		return DB_FAILED;
	}
}
