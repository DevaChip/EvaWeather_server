package com.devachip.evaweather.bean;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.devachip.evaweather.vo.LocationInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * API 조회에 필요한 데이터를 담고 있는 클래스
 * Spring Bean 등록하여 서버 실행 시, 정보를 로딩하여 담고 있도록 한다.
 * 
 * 1. 행정구역코드 및 지역 정보(동네예보, 동네예보 통보문, 중기예보 API 조회 시, 사용됨.)
 * 
 * @author idean
 * @since 2020.12.10
 */
@Slf4j
@Component
public class DataBean {
	// 시트 구분
	final int LOCATION_INFO_SHEET=0; // 지역 정보
	
	// 행 구분
	final int LANG=0; 		// 구분
	final int AREA_CODE=1;	// 행정구역코드
	final int FIRST_AREA=2;	// 1단계 
	final int SECOND_AREA=3;	// 2단계
	final int THIRD_AREA=4;	// 3단계
	final int NX=5;			// 격자 X
	final int NY=6;			// 격자 Y
	final int LONGITUDE_H=7;// 경도(시)
	final int LONGITUDE_M=8;// 경도(분)
	final int LONGITUDE_S=9;// 경도(초)
	final int LATITUDE_H=10;// 위도(시)
	final int LATITUDE_M=11;// 위도(분)
	final int LATITUDE_S=12;// 위도(초)
	final int LONGITUDE_S_PER_HUNDRED=13;// 경도 (초/100)
	final int LATITUDE_S_PER_HUNDRED=14;	// 위도 (초/100)
	final int LOCATION_UPDATE=15;		// 위치업데이트	
	
	// NX, NY 최대값 +1
	// 편의를 위해 +1 하여 최대값까지 인덱스로 사용할 수 있도록 설정함
	final int NX_MAX = 145;
	final int NY_MAX = 148;
	
	private Map<String, LocationInfo> locationInfoMap;	// 지역 정보
	private List<LocationInfo> locationInfoList_schedule;	// nx, ny 중복값을 제거한 정보
	
	public DataBean() {
		if (locationInfoMap != null) {	// 이미 로딩된 경우 생략
			return;
		}
		loadLocationInfoMap();
	}
	
	public Map<String, LocationInfo> getLocationInfoMap() {
		return locationInfoMap;
	}
	
	public List<LocationInfo> getLocationInfoList_schedule() {
		return locationInfoList_schedule;
	}
	
	private synchronized void loadLocationInfoMap() {
		locationInfoMap = new HashMap<>();
		
		// 스케줄 전용 데이터 -> 중복되는 nx, ny 값을 제거하여 API 호출 횟수를 줄이기 위해 추가함.
		locationInfoList_schedule = new ArrayList<>();
		LocationInfo[][] scheduleArr = new LocationInfo[NX_MAX][NY_MAX];
		
		XSSFWorkbook wb = null;
		
		log.debug("==================== load LocationInfo Start ====================");
		try {
			String fileName = "LocationInfo.xlsx";
			ClassPathResource resource = new ClassPathResource(fileName);
			InputStream is = resource.getInputStream();
			
			// .xlsx 파일의 행,열이 너무 많을 경우 Zip bomb 공격으로 인식함.
			// 이를 해결하기 위해 아래 설정 추가
			ZipSecureFile.setMinInflateRatio(0);
			
			wb = new XSSFWorkbook(is);
			XSSFSheet sheet = wb.getSheetAt(LOCATION_INFO_SHEET);
			Row colNamesRow = sheet.getRow(0);
			
			String[] cells = new String[colNamesRow.getLastCellNum()];	// row 당 셀 최대 갯수(컬럼명 row는 빈 값이 없기 때문에 최대 갯수가 된다.)
			
			sheet.removeRow(colNamesRow);
			log.debug("Sheet rows: " + sheet.getLastRowNum());
			
			for (Row row : sheet) {
				if (row==null) {
					log.debug("row Data is Null.");
					continue;
				}
				
				Arrays.fill(cells, "");
				
				for (int i=0; i < cells.length; i++) {
					Cell cell = row.getCell(i);
					
					switch(Optional.ofNullable(cell).map(Cell::getCellType).orElse(CellType.BLANK)) {
					case NUMERIC:
						cells[i] = new BigDecimal(cell.getNumericCellValue()).toString();
						break;
					case STRING:
						cells[i] = cell.getStringCellValue();
						break;
					case FORMULA:
						cells[i] = cell.getCellFormula();
						break;
					case BOOLEAN:
						cells[i] = String.valueOf(cell.getBooleanCellValue());
						break;
					case BLANK:
					case ERROR:
					default:
						cells[i] = "";
					}
				}
				
				// 숫자형 문자를 숫자로 인식하는 경우가 있다.
				// 이 경우 정수에 소숫점이 붙어 잘못된 값이 나올 수 있기 때문에 정수형 문자열인 경우 소숫점을 제거한다.
				int nx = (int)Double.parseDouble(cells[NX]);
				int ny = (int)Double.parseDouble(cells[NY]);
				
				cells[NX] = String.valueOf(nx);
				cells[NY] = String.valueOf(ny);
				
				LocationInfo krAreaCode = new LocationInfo(
						cells[LANG], cells[AREA_CODE], cells[FIRST_AREA], cells[SECOND_AREA], cells[THIRD_AREA],
						cells[NX], cells[NY], cells[LONGITUDE_H], cells[LONGITUDE_M], cells[LONGITUDE_S],
						cells[LATITUDE_H], cells[LATITUDE_M], cells[LATITUDE_S],
						cells[LONGITUDE_S_PER_HUNDRED], cells[LATITUDE_S_PER_HUNDRED], cells[LOCATION_UPDATE]);
				
				locationInfoMap.put(cells[AREA_CODE], krAreaCode);
				scheduleArr[nx][ny] = krAreaCode;
			}
			log.debug("inserted Rows: " + locationInfoMap.size());
			
			for (int nx=0; nx < NX_MAX; nx++) {
				for (int ny=0; ny < NY_MAX; ny++) {
					if (scheduleArr[nx][ny]==null) {
						continue;
					}
					
					locationInfoList_schedule.add(scheduleArr[nx][ny]);
				}
			}
			log.debug("inserted Schedule Rows: " + locationInfoList_schedule.size());
		} catch(Exception e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			// AutoCloseable 때문에 닫을 필요 없이 자원이 회수된다.
			// warning 표시를 지우기 위해 추가함.
			IOUtils.closeQuietly(wb);
		}
		
		log.debug("==================== load LocationInfo End ====================");
	}
}
