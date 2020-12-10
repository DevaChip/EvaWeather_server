package com.devachip.evaweather.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DatabaseInfo {
	protected String driverClassName;
	protected String user;
	protected String pw;
	protected String url;
}
