package com.devachip.evaweather.clothes;

import org.apache.commons.lang3.StringUtils;

/**
 * 크롤링 기능을 관리하기 위해 팩토리 패턴 구현 중.
 * - 공부 필요.
 * 
 * @author dykim
 * @since 2021.01.13
 */
public class WebCrawlingFactory {
	public static Crawler getInstance(String siteName) {
		if (StringUtils.equals("무신사", siteName)) {
			return new MusinsaCrawler();
		}
		
		return null;
	}
}
