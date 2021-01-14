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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.devachip.evaweather.bean.DataBean;
import com.devachip.evaweather.domain.UltraSrtNcst;
import com.devachip.evaweather.dto.VilageFcstRequest;
import com.devachip.evaweather.persistence.UltraSrtNcstDAO;
import com.devachip.evaweather.persistence.UltraSrtNcstDAOImpl;
import com.devachip.evaweather.util.BeanUtils;
import com.devachip.evaweather.vo.LocationInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 초단기실황 데이터 수집 스케쥴러
 */
@Slf4j
public class JobUltraSrtNcst extends QuartzJobBean {
	private static final String SERVICE_KEY = "5U%2F51omK%2FH%2F1Qf3TZG9f0QkCSHP9fpI9cAWdjV3xScZ6Sj9QFn4WL7pe8YldzB%2BZjrD1fVBrbNTS2pMDj6siAw%3D%3D";
	
	private final int DB_FAILED = 0;
	private final int DB_INSERTED = 1;
	private final int DB_UPDATED = 2;
	
	private final int CONNECT_TIMEOUT = 10000;
	private final int READ_TIMEOUT = 10000;
	
	private StringBuffer sb = new StringBuffer();
	
	// TODO : Autowired 할 수 있도록 SchedulerFactoryBean 코드로 구현하여 ApplicationContext 설정하기
	private UltraSrtNcstDAO dao = (UltraSrtNcstDAO) BeanUtils.getBean(UltraSrtNcstDAOImpl.class);
	private DataBean dataBean = (DataBean) BeanUtils.getBean(DataBean.class);
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		String jobName = context.getJobDetail().getKey().getName();
		String jobDetail = context.getJobDetail().getKey().getName();
		
		log.debug("===================== [{}] START =====================", jobName);
		getUltraSrtNcsts(jobDetail);
		log.debug(sb.toString());
		log.debug("===================== [{}] END =====================", jobName);
	}
	
	// 초단기실황 업데이트 | 추가
	public void getUltraSrtNcsts(String jobDetail) {
		// 현재 시간
		DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
		DateFormat tFormat = new SimpleDateFormat("HHmm");

		Date d = new Date();
		
		String currentDate = dFormat.format(d);
		String currentTime = tFormat.format(d);
		
		DateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String nowTime = timeFormat.format(d);
		sb.append(String.format("[%s][Scheduler] %s Start", nowTime, jobDetail)).append("\n");
		
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
			String apiName = "getUltraSrtNcst";
			String getResult = getVilageFcstData(apiName, request);
			
			if (getResult==null) {	// API 통신에 실패한 경우
				sb.append(String.format("(%s, %s) Failed to receive response from server. Update Skip.", info.getNx(), info.getNy())).append("\n");
				failedRows++;
				continue;
			}
			
			// API로부터 받은 데이터 파싱
			Map<String, Object> resultMap = jsonToObject(getResult);
			
			// 에러 코드 분류
			String resultCode = (String) resultMap.get("resultCode");
			if (StringUtils.equals(resultCode, "03")) {
				sb.append("No data has been generated for the current time. Job Stop.").append("\n");
				break;
			} else if (!StringUtils.equals(resultCode, "00")) {
				sb.append(String.format("(%s, %s) %s. Update Skip.", info.getNx(), info.getNy(), (String) resultMap.get("resultMsg"))).append("\n");
				failedRows++;
				continue;
			}
				
			// 데이터 업데이트 | 삽입
			UltraSrtNcst entity = (UltraSrtNcst)resultMap.get("entity");
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
				sb.append(String.format("(%s, %s) DB update Failed.", info.getNx(), info.getNy())).append("\n");
			}
		}
		
		sb.append(String.format("AllRows: %d, updatedRows: %d, insertedRows: %d, failedRows: %d",
				dataBean.getLocationInfoList_schedule().size(), updatedRows, insertedRows, failedRows)).append("\n");
		
		Date afterD = new Date();
		String afterTime = timeFormat.format(afterD);
		sb.append(String.format("[%s][Scheduler] %s End", afterTime, jobDetail)).append("\n");
		
		long runTime = (afterD.getTime() - d.getTime())/1000;
		sb.append(String.format("runTime: %dm %ds", runTime/60, runTime%60));
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
			sb.append(e.fillInStackTrace()).append("\n");
		} catch (IOException e) {
			sb.append(e.fillInStackTrace()).append("\n");
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
		
		try {
			Map<String, Object> map = mapper.readValue(jsonString, Map.class);
			
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
				
				dtoMap.put((String) item.get("category"), Float.parseFloat((String) item.get("obsrValue")));
			}
			
			UltraSrtNcst entity = new UltraSrtNcst((String) dtoMap.get("baseDate"), (String) dtoMap.get("baseTime"), (int) dtoMap.get("nx"), (int) dtoMap.get("ny"),
					(float) dtoMap.get("PTY"), (float) dtoMap.get("REH"), (float) dtoMap.get("RN1"), (float) dtoMap.get("T1H"),
					(float) dtoMap.get("UUU"), (float) dtoMap.get("VEC"), (float) dtoMap.get("VVV"), (float) dtoMap.get("WSD")); 
			
			resultMap.put("entity", entity);
			return resultMap;
		} catch(IOException e) {
			sb.append(e.fillInStackTrace()).append("\n");
			resultMap = new HashMap<>();
		} catch(Exception e) {
			sb.append(e.fillInStackTrace()).append("\n");
			resultMap = new HashMap<>();
		}
		
		return resultMap;
	}
	
	/**
	 * API 데이터 갱신 | 삽입
	 * 
	 * @param entity
	 * @return DB 작업 코드값 {0:실패, 1:삽입, 2: 갱신}
	 */
	public synchronized int updateData(UltraSrtNcst entity) {
		if (entity == null) {
			return DB_FAILED;
		}
		
		if (dao.update(entity) ==1) {
			return DB_UPDATED;
		}
		
		if (dao.insert(entity)==1) {
			return DB_INSERTED;
		}
		
		return DB_FAILED;
	}
}
