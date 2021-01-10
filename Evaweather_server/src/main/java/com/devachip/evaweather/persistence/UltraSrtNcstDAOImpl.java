package com.devachip.evaweather.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.devachip.evaweather.bean.DBConnect;
import com.devachip.evaweather.domain.UltraSrtNcst;
import com.devachip.eveweather.mapper.UltraSrtNcstMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class UltraSrtNcstDAOImpl implements UltraSrtNcstDAO {
	private DBConnect dbConnect;
	private UltraSrtNcstMapper mapper = new UltraSrtNcstMapper();
	
	@Autowired
	public UltraSrtNcstDAOImpl(DBConnect dbConnect) {
		this.dbConnect = dbConnect;
	}
	
	@Override
	public int update(UltraSrtNcst dto) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getUpdateSQL());
			psmt.setFloat(1, dto.getT1H());
			psmt.setFloat(2, dto.getRN1());
			psmt.setFloat(3, dto.getUUU());
			psmt.setFloat(4, dto.getVVV());
			psmt.setFloat(5, dto.getREH());
			psmt.setFloat(6, dto.getPTY());
			psmt.setFloat(7, dto.getVEC());
			psmt.setFloat(8, dto.getWSD());
			
			psmt.setString(9, dto.getBaseDate());
			psmt.setString(10, dto.getBaseTime());
			psmt.setInt(11, dto.getNx());
			psmt.setInt(12, dto.getNy());			
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		}
		
		return 0;
	}

	@Override
	public int insert(UltraSrtNcst dto) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getInsertSQL());
			psmt.setString(1, dto.getBaseDate());
			psmt.setString(2, dto.getBaseTime());
			psmt.setInt(3, dto.getNx());
			psmt.setInt(4, dto.getNy());
			
			psmt.setFloat(5, dto.getT1H());
			psmt.setFloat(6, dto.getRN1());
			psmt.setFloat(7, dto.getUUU());
			psmt.setFloat(8, dto.getVVV());
			psmt.setFloat(9, dto.getREH());
			psmt.setFloat(10, dto.getPTY());
			psmt.setFloat(11, dto.getVEC());
			psmt.setFloat(12, dto.getWSD());		
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		}
		
		return 0;
	}

}
