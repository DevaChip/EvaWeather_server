package com.devachip.evaweather.base;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;

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
}
