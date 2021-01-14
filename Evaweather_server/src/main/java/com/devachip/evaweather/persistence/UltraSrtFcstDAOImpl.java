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
	public int update(UltraSrtFcst dto) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getUpdateSQL());
			psmt.setFloat(1, dto.getT1H());
			psmt.setFloat(2, dto.getRN1());
			psmt.setFloat(3, dto.getSKY());
			psmt.setFloat(4, dto.getUUU());
			psmt.setFloat(5, dto.getVVV());
			psmt.setFloat(6, dto.getREH());
			psmt.setFloat(7, dto.getPTY());
			psmt.setFloat(8, dto.getLGT());
			psmt.setFloat(9, dto.getVEC());
			psmt.setFloat(10, dto.getWSD());
			
			psmt.setString(11, dto.getFcstDate());
			psmt.setString(12, dto.getFcstTime());
			psmt.setInt(13, dto.getNx());
			psmt.setInt(14, dto.getNy());			
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			DBConnect.close(psmt);
		}
		
		return 0;
	}

	@Override
	public int insert(UltraSrtFcst dto) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getInsertSQL());
			psmt.setString(1, dto.getFcstDate());
			psmt.setString(2, dto.getFcstTime());
			psmt.setInt(3, dto.getNx());
			psmt.setInt(4, dto.getNy());
			
			psmt.setFloat(5, dto.getT1H());
			psmt.setFloat(6, dto.getRN1());
			psmt.setFloat(7, dto.getSKY());
			psmt.setFloat(8, dto.getUUU());
			psmt.setFloat(9, dto.getVVV());
			psmt.setFloat(10, dto.getREH());
			psmt.setFloat(11, dto.getPTY());
			psmt.setFloat(12, dto.getLGT());
			psmt.setFloat(13, dto.getVEC());
			psmt.setFloat(14, dto.getWSD());			
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			DBConnect.close(psmt);
		}
		
		return 0;
	}

}
