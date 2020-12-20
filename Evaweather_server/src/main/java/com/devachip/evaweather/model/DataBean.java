package com.devachip.evaweather.model;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
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


/**
 * API 조회에 필요한 데이터를 담고 있는 클래스
 * Spring Bean 등록하여 싱글톤으로 관리되도록 한다.
 * 
 * 1. 행정구역코드 및 지역 정보(동네예보, 동네예보 통보문, 중기예보 API 조회 시, 사용됨.)
 * 
 * @author idean
 * @since 2020.12.10
 */
@Component
public class DataBean {
	private static Map<String, LocationInfo> locationInfoMap;	// 지역 정보
	
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
	
	public DataBean() {
		loadLocationInfoMap();
	}
	
	public static Map<String, LocationInfo> getLocationInfoMap() {
		return locationInfoMap;
	}
	
	private void loadLocationInfoMap() {
		if (locationInfoMap != null) {	// 이미 로딩된 경우 생략
			return;
		}
		locationInfoMap = new HashMap<>();
		XSSFWorkbook wb = null;
		
		try {
			String fileName = "LocationInfo.xlsx";
			ClassPathResource resource = new ClassPathResource(fileName);
			InputStream is = resource.getInputStream();
			
			// .xlsx 파일의 행,열이 너무 많을 경우 Zip bomb 공격으로 인식함.
			// 이를 해결하기 위해 아래 설정 추가
			ZipSecureFile.setMinInflateRatio(0);	 
			
			wb = new XSSFWorkbook(is);
			XSSFSheet sheet = wb.getSheetAt(LOCATION_INFO_SHEET);
			
			Row columnNames = sheet.getRow(0);
			String[] cells = new String[columnNames.getLastCellNum()];	// row 당 셀 최대 갯수
			
			sheet.removeRow(columnNames);	// 첫 행(컬럼명) 삭제
			for (Row row: sheet) {
				Arrays.fill(cells, "");
				
				for (int i=0; i < row.getLastCellNum(); i++) {
					Cell cell = row.getCell(i);
					
					switch(Optional.ofNullable(cell).map(Cell::getCellType).orElse(CellType.BLANK)) {
					case NUMERIC:
						cells[i] = String.valueOf(cell.getNumericCellValue());
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
				int areaCode = (int)Double.parseDouble(cells[AREA_CODE]);
				int nx = (int)Double.parseDouble(cells[NX]);
				int ny = (int)Double.parseDouble(cells[NY]);
				
				cells[AREA_CODE] = String.valueOf(areaCode);
				cells[NX] = String.valueOf(nx);
				cells[NY] = String.valueOf(ny);
				
				LocationInfo krAreaCode = new LocationInfo(
						cells[LANG], cells[AREA_CODE], cells[FIRST_AREA], cells[SECOND_AREA], cells[THIRD_AREA],
						cells[NX], cells[NY], cells[LONGITUDE_H], cells[LONGITUDE_M], cells[LONGITUDE_S],
						cells[LATITUDE_H], cells[LATITUDE_M], cells[LATITUDE_S],
						cells[LONGITUDE_S_PER_HUNDRED], cells[LATITUDE_S_PER_HUNDRED], cells[LOCATION_UPDATE]);
				locationInfoMap.put(cells[AREA_CODE], krAreaCode);
			}
		} catch(Exception e) {
			System.out.println(e.fillInStackTrace());
		} finally {
			if (wb!=null) {
				// AutoCloseable 때문에 닫을 필요 없이 자원이 회수된다.
				// warning 표시를 지우기 위해 추가함.
				IOUtils.closeQuietly(wb);
			}
		}
	}
}
