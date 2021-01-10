package com.devachip.evaweather.bean;

import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Component;

import com.devachip.evaweather.base.PropertiesConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DBConnect {
	private Connection conn;
	private PropertiesConfig properties;
	
	public DBConnect(PropertiesConfig properties) {
		this.properties = properties;
		dbConnect();
	}
	
	public Connection getConnection() {
		return conn;
	}

	public void dbConnect() {
		if (conn != null) {
			return;
		}

		BufferedReader rd = null;
		try {
			Class.forName(properties.getDb_className());
			conn = DriverManager.getConnection(properties.getDb_url(), properties.getDb_userName(), properties.getDb_password());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(rd);
		}
	}
	
	public static boolean close(Object obj) {
		if (obj==null) {
			return true;
		}
		
		try {
			if (obj instanceof Connection) {
				((Connection) obj).close();
				return true;
			}
			
			if (obj instanceof PreparedStatement) {
				((PreparedStatement) obj).close();
			}
			
			if (obj instanceof ResultSet) {
				((ResultSet) obj).close();
			}
			
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		}
		
		return false;
	}
}
