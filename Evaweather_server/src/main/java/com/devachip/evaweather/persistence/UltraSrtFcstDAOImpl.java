package com.devachip.evaweather.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.stereotype.Repository;

import com.devachip.evaweather.bean.DBConnect;
import com.devachip.evaweather.domain.UltraSrtFcst;
import com.devachip.eveweather.mapper.UltraSrtFcstMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class UltraSrtFcstDAOImpl implements UltraSrtFcstDAO {
	private DBConnect dbConnect;
	private UltraSrtFcstMapper mapper = new UltraSrtFcstMapper();
	
	public UltraSrtFcstDAOImpl(DBConnect dbConnect) {
		this.dbConnect = dbConnect;
	}

	@Override
	public int update(UltraSrtFcst entity) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getUpdateSQL());
			psmt.setFloat(1, entity.getT1H());
			psmt.setFloat(2, entity.getRN1());
			psmt.setFloat(3, entity.getSKY());
			psmt.setFloat(4, entity.getUUU());
			psmt.setFloat(5, entity.getVVV());
			psmt.setFloat(6, entity.getREH());
			psmt.setFloat(7, entity.getPTY());
			psmt.setFloat(8, entity.getLGT());
			psmt.setFloat(9, entity.getVEC());
			psmt.setFloat(10, entity.getWSD());
			
			psmt.setString(11, entity.getFcstDate());
			psmt.setString(12, entity.getFcstTime());
			psmt.setInt(13, entity.getNx());
			psmt.setInt(14, entity.getNy());			
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			DBConnect.close(psmt);
		}
		
		return -1;
	}

	@Override
	public int insert(UltraSrtFcst entity) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getInsertSQL());
			psmt.setString(1, entity.getFcstDate());
			psmt.setString(2, entity.getFcstTime());
			psmt.setInt(3, entity.getNx());
			psmt.setInt(4, entity.getNy());
			
			psmt.setFloat(5, entity.getT1H());
			psmt.setFloat(6, entity.getRN1());
			psmt.setFloat(7, entity.getSKY());
			psmt.setFloat(8, entity.getUUU());
			psmt.setFloat(9, entity.getVVV());
			psmt.setFloat(10, entity.getREH());
			psmt.setFloat(11, entity.getPTY());
			psmt.setFloat(12, entity.getLGT());
			psmt.setFloat(13, entity.getVEC());
			psmt.setFloat(14, entity.getWSD());			
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			DBConnect.close(psmt);
		}
		
		return -1;
	}

	@Override
	public int delete(UltraSrtFcst entity) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getDeleteSQL());
			psmt.setString(1, entity.getFcstDate());
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			DBConnect.close(psmt);
		}
		
		return -1;
	}

}
