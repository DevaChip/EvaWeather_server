package com.devachip.evaweather.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
	
	public UltraSrtNcstDAOImpl(DBConnect dbConnect) {
		this.dbConnect = dbConnect;
	}
	
	@Override
	public int update(UltraSrtNcst entity) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getUpdateSQL());
			psmt.setFloat(1, entity.getT1H());
			psmt.setFloat(2, entity.getRN1());
			psmt.setFloat(3, entity.getUUU());
			psmt.setFloat(4, entity.getVVV());
			psmt.setFloat(5, entity.getREH());
			psmt.setFloat(6, entity.getPTY());
			psmt.setFloat(7, entity.getVEC());
			psmt.setFloat(8, entity.getWSD());
			
			psmt.setString(9, entity.getBaseDate());
			psmt.setString(10, entity.getBaseTime());
			psmt.setInt(11, entity.getNx());
			psmt.setInt(12, entity.getNy());			
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		}
		
		return -1;
	}

	@Override
	public int insert(UltraSrtNcst entity) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getInsertSQL());
			psmt.setString(1, entity.getBaseDate());
			psmt.setString(2, entity.getBaseTime());
			psmt.setInt(3, entity.getNx());
			psmt.setInt(4, entity.getNy());
			
			psmt.setFloat(5, entity.getT1H());
			psmt.setFloat(6, entity.getRN1());
			psmt.setFloat(7, entity.getUUU());
			psmt.setFloat(8, entity.getVVV());
			psmt.setFloat(9, entity.getREH());
			psmt.setFloat(10, entity.getPTY());
			psmt.setFloat(11, entity.getVEC());
			psmt.setFloat(12, entity.getWSD());		
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		}
		
		return -1;
	}

	@Override
	public int delete(UltraSrtNcst entity) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getDeleteSQL());
			psmt.setString(1, entity.getBaseDate());
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		}
		
		return -1;
	}

}
