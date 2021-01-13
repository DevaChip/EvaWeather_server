package com.devachip.evaweather.clothes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.devachip.evaweather.dto.NowWeather_Clothes;

/**
 * 무신사 크롤링 구현
 * 
 * @author dykim
 * @since 2021.01.13
 */
public class MusinsaCrawler implements Crawler{
	private Map<String, Object> itemList;
	
	public MusinsaCrawler() {
		setItemList();
	}

	private void setItemList() {
		itemList = new HashMap<>();
		itemList.put("상의", "C:\\Git\\Web-Crawler\\clothes\\무신사\\상의\\itemData.xlsx");
	}
	
	@Override
	public Map<String, Object> getItemList() {
		return itemList;
	}

	@Override
	public List<NowWeather_Clothes> getClothes() {
		InputStream isData = null;
		InputStream isImg = null;
		
		XSSFWorkbook wb = null;
		
		try {
			byte[] imgBytes = null;
			List<NowWeather_Clothes> clothes = new ArrayList<>();
			
			Set<String> categoryList = itemList.keySet();
			for (String category: categoryList) {
				String path = (String) itemList.get(category);
				isData = new BufferedInputStream(new FileInputStream(path));
				
				wb = new XSSFWorkbook(isData);
				XSSFSheet sheet = wb.getSheetAt(0);
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
					
					// TODO : 
				}
			}
			
			// 아우터
			String outerPath = basePath + File.separator + "outer.jpg";
			
			isImg = new BufferedInputStream(new FileInputStream(outerPath));
			imgBytes = IOUtils.toByteArray(isImg);
			
			NowWeather_Clothes outer = new NowWeather_Clothes();
			outer.setImg(imgBytes);
			outer.setLink("TODO");
			
			clothes.add(outer);
			
			dto.setClothes(clothes);
			
			return true;
		} catch (FileNotFoundException e) {
			log.error(e.fillInStackTrace() + "");
		} catch (IOException e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			IOUtils.closeQuietly(isImg);
			IOUtils.closeQuietly(isData);
			
			IOUtils.closeQuietly(wb);
		}
	}
}
