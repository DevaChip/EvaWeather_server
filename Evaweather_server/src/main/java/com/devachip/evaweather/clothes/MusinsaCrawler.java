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
import java.util.Random;
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
		
		String[] categoryList = {"아우터", "상의", "바지", "신발"};
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
			for (String category: categoryList) {
				List<String[]> clothesList = new ArrayList<>();
				
				String path = (String) itemList.get(category);
				isData = new BufferedInputStream(new FileInputStream(path));
				
				wb = new XSSFWorkbook(isData);
				XSSFSheet sheet = wb.getSheetAt(SHEET1);
				Row colNamesRow = sheet.getRow(0);
				
				sheet.removeRow(colNamesRow);	// 첫 줄 제외(컬럼명)
				
				for (Row row : sheet) {
					if (row==null) {
						log.debug("row Data is Null.");
						continue;
					}
					
					String[] cells = new String[row.getLastCellNum()];
					
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
				
				// 옷 고르기
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
	
	// 클라이언트로 전달할 옷 고르기
	private List<String[]> chooseClothes(List<String[]> clothesList) {
		try {
			Random rand = new Random();
			rand.setSeed(System.currentTimeMillis());
			
			while(clothesList.size()>3) {
				for (int i=0; i<clothesList.size(); i++) {
					double random = rand.nextDouble();
					if (random < 0.03 || 0.95 < random ) {
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
