package com.devachip.evaweather.clothes;

import java.util.HashMap;
import java.util.Map;

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
	
	public Map<String, Object> getItemList() {
		return itemList;
	}
}
