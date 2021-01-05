package com.devachip.evaweather.bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DBConnect {
	private static Connection conn;
	
	private final int CLASS_NAME = 0;
	private final int USER_NAME = 1;
	private final int PASSWORD = 2;
	private final int URL = 3;

	public DBConnect() {
		dbConnect();
	}
	
	public static Connection getConnection() {
		return conn;
	}

	public void dbConnect() {
		if (conn != null) {
			return;
		}

		String fileName = "dbConnection.txt";
		ClassPathResource resource = new ClassPathResource(fileName);
		InputStream is = null;
		BufferedReader rd = null;
		try {
			// 파일로부터 DB 정보 읽기
			is = resource.getInputStream();
			rd = new BufferedReader(new InputStreamReader(is));

			String line;
			String[] args = new String[4];
			while ((line = rd.readLine()) != null) {
				String[] configs = StringUtils.split(line, "\\");

				switch (configs[0]) {
				case "db.className":
					args[CLASS_NAME] = configs[1];
					break;
				case "db.userName":
					args[USER_NAME] = configs[1];
					break;
				case "db.password":
					args[PASSWORD] = configs[1];
					break;
				case "db.url":
					args[URL] = configs[1];
					break;
				}
			}

			// DB 연결
			Class.forName(args[CLASS_NAME]);
			conn = DriverManager.getConnection(args[URL], args[USER_NAME], args[PASSWORD]);
		} catch (IOException e) {
			e.printStackTrace();
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
