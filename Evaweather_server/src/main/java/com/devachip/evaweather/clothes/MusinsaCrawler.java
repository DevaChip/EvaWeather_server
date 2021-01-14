package com.devachip.evaweather.clothes;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.devachip.evaweather.base.PropertiesConfig;
import com.devachip.evaweather.dto.NowWeather_Clothes;
import com.devachip.evaweather.util.BeanUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 무신사 크롤링 구현
 * 
 * @author dykim
 * @since 2021.01.13
 */
@Slf4j
public class MusinsaCrawler implements Crawler{
	private Map<String, Object> itemList;
	private PropertiesConfig properties = (PropertiesConfig) BeanUtils.getBean(PropertiesConfig.class);
	private String siteName = "무신사";
	
	// 시트 구분
	final int SHEET1=0; // 지역 정보
	
	// 행 구분
	final int INDEX = 0;
	final int CODE = 1;
	final int CATEGORY = 2;
	final int DETAIL = 3;
	final int NAME = 4;
	final int LINK = 5;
	final int SEASON = 6;
	
	public MusinsaCrawler() {
		setItemList();
	}

	private void setItemList() {
		itemList = new HashMap<>();
		
		String[] categoryList = {"상의"};
		for (String category: categoryList) {
			String path = Paths.get(properties.getClothes_path(), siteName, category, "itemData.xlsx").toString();
			itemList.put(category, path);
		}
	}
	
	@Override
	public Map<String, Object> getItemList() {
		return itemList;
	}

	@Override
	public List<NowWeather_Clothes> getClothes(String season) {
		String basePath = properties.getClothes_path();
		InputStream isData = null;
		InputStream isImg = null;
		
		XSSFWorkbook wb = null;
		
		try {
			byte[] imgBytes = null;
			List<NowWeather_Clothes> clothes = new ArrayList<>();
			
			Set<String> categoryList = itemList.keySet();
			List<String[]> clothesList = new ArrayList<>();
			for (String category: categoryList) {
				String path = (String) itemList.get(category);
				isData = new BufferedInputStream(new FileInputStream(path));
				
				wb = new XSSFWorkbook(isData);
				XSSFSheet sheet = wb.getSheetAt(SHEET1);
				Row colNamesRow = sheet.getRow(0);
				
				String[] cells = new String[colNamesRow.getLastCellNum()];	// row 당 셀 최대 갯수(컬럼명 row는 빈 값이 없기 때문에 최대 갯수가 된다.)
				
				sheet.removeRow(colNamesRow);	// 첫 줄 제외(컬럼명)
				
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
					
					// 가져온 옷 중 계절에 맞는 옷만 추가
					if (StringUtils.contains(cells[SEASON], season)) {
						clothesList.add(cells);
					}
				}
				
				printClothesList(clothesList);
				
				// 옷 추리기
				clothesList = chooseClothes(clothesList);
				
				printClothesList(clothesList);
				
				// 항목에 대한 이미지, 링크 가져오기
				for(String[] clothesData: clothesList) {
					String clothesPath = Paths.get(basePath, siteName, category, clothesData[CODE] + ".jpg").toString();
					isImg = new BufferedInputStream(new FileInputStream(clothesPath));
					imgBytes = IOUtils.toByteArray(isImg);
					
					NowWeather_Clothes categoryClothes = new NowWeather_Clothes();
					categoryClothes.setImg(imgBytes);
					categoryClothes.setLink(clothesData[LINK]);
					
					clothes.add(categoryClothes);
				}
			}
			
			return clothes;
		} catch (FileNotFoundException e) {
			log.error(e.fillInStackTrace() + "");
		} catch (IOException e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			IOUtils.closeQuietly(isImg);
			IOUtils.closeQuietly(isData);
			
			IOUtils.closeQuietly(wb);
		}
		
		return null;
	}
	
	// 전체 목록 중 최대 5개 항목으로 추리기
	private List<String[]> chooseClothes(List<String[]> clothesList) {
		try {
			while(clothesList.size()>5) {
				for (int i=0; i<clothesList.size(); i++) {
					double random = Math.random();
					if (random < 0.6) {
						clothesList.remove(i);
						break;
					}
				}
			}
			
			return clothesList;
		} catch (Exception e) {
			log.error(e.fillInStackTrace() + "");
		}		
		
		return null;
	}
	
	private void printClothesList(List<String[]> list) {
		log.debug("====================================");
		for(String[] item: list) {
			log.debug(Arrays.toString(item));
		}
		log.debug("====================================\n");
	}
}
