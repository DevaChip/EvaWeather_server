package com.devachip.evaweather.dbconnect;

import java.sql.Connection;

import com.devachip.evaweather.vo.DatabaseInfo;

public class DBConnect {
	static DBConnect instance;
	public static DBConnect getInstance() {
		instance = new DBConnect();
		return instance;
	}
	
	public Connection dbconnect() {
		DatabaseInfo databaseInfo = ServiceMng.servuce
	}
}
