package com.devachip.evaweather.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.devachip.evaweather.bean.DBConnect;
import com.devachip.evaweather.domain.VilageFcst;
import com.devachip.eveweather.mapper.VilageFcstMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class VilageFcstDAOImpl implements VilageFcstDAO {
	private DBConnect dbConnect;
	private VilageFcstMapper mapper = new VilageFcstMapper();
	
	@Autowired
	public VilageFcstDAOImpl(DBConnect dbConnect) {
		this.dbConnect = dbConnect;
	}
	
	@Override
	public int update(VilageFcst dto) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getUpdateSQL());
			psmt.setFloat(1, dto.getPOP());
			psmt.setFloat(2, dto.getPTY());
			setFloat(psmt, 3, dto.getR06());
			psmt.setFloat(4, dto.getREH());
			setFloat(psmt, 5, dto.getS06());
			psmt.setFloat(6, dto.getSKY());
			psmt.setFloat(7, dto.getT3H());
			setFloat(psmt, 8, dto.getTMN());
			setFloat(psmt, 9, dto.getTMX());
			psmt.setFloat(10, dto.getUUU());
			psmt.setFloat(11, dto.getVVV());
			setFloat(psmt, 12, dto.getWAV());
			psmt.setFloat(13, dto.getVEC());
			psmt.setFloat(14, dto.getWSD());
			
			psmt.setString(15, dto.getFcstDate());
			psmt.setString(16, dto.getFcstTime());
			psmt.setInt(17, dto.getNx());
			psmt.setInt(18, dto.getNy());
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			DBConnect.close(psmt);
		}
		
		return 0;
	}

	@Override
	public int insert(VilageFcst dto) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getInsertSQL());
			psmt.setString(1, dto.getFcstDate());
			psmt.setString(2, dto.getFcstTime());
			psmt.setInt(3, dto.getNx());
			psmt.setInt(4, dto.getNy());
			
			psmt.setFloat(5, dto.getPOP());
			psmt.setFloat(6, dto.getPTY());
			setFloat(psmt, 7, dto.getR06());
			psmt.setFloat(8, dto.getREH());
			setFloat(psmt, 9, dto.getS06());
			psmt.setFloat(10, dto.getSKY());
			psmt.setFloat(11, dto.getT3H());
			setFloat(psmt, 12, dto.getTMN());
			setFloat(psmt, 13, dto.getTMX());
			psmt.setFloat(14, dto.getUUU());
			psmt.setFloat(15, dto.getVVV());
			setFloat(psmt, 16, dto.getWAV());
			psmt.setFloat(17, dto.getVEC());
			psmt.setFloat(18, dto.getWSD());
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			DBConnect.close(psmt);
		}
		
		return 0;
	}

	private PreparedStatement setFloat(PreparedStatement psmt, int idx, Float value) {
		try {
			if (value != null) {
				psmt.setFloat(idx, value.floatValue());
			} else {
				psmt.setNull(idx, Types.DECIMAL);
			}
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		}
		
		return psmt;
	}
}
