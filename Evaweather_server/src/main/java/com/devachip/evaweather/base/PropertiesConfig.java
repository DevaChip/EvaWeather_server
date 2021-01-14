package com.devachip.evaweather.base;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;

/**
 * .properties 값을 가지는 클래스
 * 설정값을 객체로 가지고 있도록 하여 코드상에서 정확한 확인을 할 수 있도록 한다.
 * 
 * @author dykim
 * @since 2021.01.10
 */
@Getter
@Configuration
@PropertySource(value="classpath:application.properties", encoding="UTF-8")
public class PropertiesConfig {
	/* DB 접속 정보*/
	@Value("${db.className}")
	String db_className;
	
	@Value("${db.userName}")
	String db_userName;
	
	@Value("${db.password}")
	String db_password;
	
	@Value("${db.url}")
	String db_url;
	
	/* 옷 정보 */
	@Value("${clothes.path}")
	String clothes_path;
	
	@Value("${clothes.siteList}")
	String[] siteList;
}
