package com.devachip.evaweather.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

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
	
	public VilageFcstDAOImpl(DBConnect dbConnect) {
		this.dbConnect = dbConnect;
	}
	
	@Override
	public int update(VilageFcst entity) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getUpdateSQL());
			psmt.setFloat(1, entity.getPOP());
			psmt.setFloat(2, entity.getPTY());
			setFloat(psmt, 3, entity.getR06());
			psmt.setFloat(4, entity.getREH());
			setFloat(psmt, 5, entity.getS06());
			psmt.setFloat(6, entity.getSKY());
			psmt.setFloat(7, entity.getT3H());
			setFloat(psmt, 8, entity.getTMN());
			setFloat(psmt, 9, entity.getTMX());
			psmt.setFloat(10, entity.getUUU());
			psmt.setFloat(11, entity.getVVV());
			setFloat(psmt, 12, entity.getWAV());
			psmt.setFloat(13, entity.getVEC());
			psmt.setFloat(14, entity.getWSD());
			
			psmt.setString(15, entity.getFcstDate());
			psmt.setString(16, entity.getFcstTime());
			psmt.setInt(17, entity.getNx());
			psmt.setInt(18, entity.getNy());
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			DBConnect.close(psmt);
		}
		
		return -1;
	}

	@Override
	public int insert(VilageFcst entity) {
		Connection conn = dbConnect.getConnection();
		PreparedStatement psmt = null;
		
		try {
			psmt = conn.prepareStatement(mapper.getInsertSQL());
			psmt.setString(1, entity.getFcstDate());
			psmt.setString(2, entity.getFcstTime());
			psmt.setInt(3, entity.getNx());
			psmt.setInt(4, entity.getNy());
			
			psmt.setFloat(5, entity.getPOP());
			psmt.setFloat(6, entity.getPTY());
			setFloat(psmt, 7, entity.getR06());
			psmt.setFloat(8, entity.getREH());
			setFloat(psmt, 9, entity.getS06());
			psmt.setFloat(10, entity.getSKY());
			psmt.setFloat(11, entity.getT3H());
			setFloat(psmt, 12, entity.getTMN());
			setFloat(psmt, 13, entity.getTMX());
			psmt.setFloat(14, entity.getUUU());
			psmt.setFloat(15, entity.getVVV());
			setFloat(psmt, 16, entity.getWAV());
			psmt.setFloat(17, entity.getVEC());
			psmt.setFloat(18, entity.getWSD());
			
			return psmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.fillInStackTrace() + "");
		} finally {
			DBConnect.close(psmt);
		}
		
		return -1;
	}
	
	@Override
	public int delete(VilageFcst entity) {
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
