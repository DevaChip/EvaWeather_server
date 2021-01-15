package com.devachip.evaweather.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.devachip.evaweather.domain.UltraSrtFcst;
import com.devachip.evaweather.domain.UltraSrtNcst;
import com.devachip.evaweather.domain.VilageFcst;
import com.devachip.evaweather.persistence.UltraSrtFcstDAO;
import com.devachip.evaweather.persistence.UltraSrtFcstDAOImpl;
import com.devachip.evaweather.persistence.UltraSrtNcstDAO;
import com.devachip.evaweather.persistence.UltraSrtNcstDAOImpl;
import com.devachip.evaweather.persistence.VilageFcstDAO;
import com.devachip.evaweather.persistence.VilageFcstDAOImpl;
import com.devachip.evaweather.util.BeanUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 오래된 날씨 정보 DB에서 삭제
 * 
 * 2일 이상 지난 정보는 삭제한다.
 * 
 * @author dykim
 * @since 2021.01.15
 */
@Slf4j
public class JobDeleteOldFcstData extends QuartzJobBean{
	private StringBuffer sb = new StringBuffer();
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		String jobName = context.getJobDetail().getKey().getName();
		String jobDetail = context.getJobDetail().getKey().getName();
		
		log.debug("===================== [{}] START =====================", jobName);
		deleteFcstData(jobDetail);
		log.debug("\n" + sb.toString());	// 실행결과 한번에 출력
		log.debug("===================== [{}] END =====================", jobName);
	}
	
	public void deleteFcstData(String jobDetail) {
		/* 삭제 기준일 설정*/
		LocalDateTime baseDate = LocalDateTime.now().minusDays(2);	// 2일전
		
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
		String deleteDate = baseDate.format(format);
		
		/* 삭제 */
		deleteUltraSrtNcst(deleteDate);	// 초단기 실황
		deleteUltraSrtFcst(deleteDate);	// 초단기 예보
		deleteVilageFcst(deleteDate);	// 동네예보 조회
	}
	
	// 초단기 실황 데이터 삭제
	private boolean deleteUltraSrtNcst(String baseDate) {
		UltraSrtNcstDAO dao = (UltraSrtNcstDAO) BeanUtils.getBean(UltraSrtNcstDAOImpl.class);
		
		UltraSrtNcst entity = new UltraSrtNcst(baseDate, "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
		int updatedRow = dao.delete(entity);
		
		if (updatedRow>-1) {
			sb.append(String.format("[%s] Data prior to base date has been successfully deleted. baseDate: %s, updatedRow: %d\n", entity.getTableName(), baseDate, updatedRow));
			return true;
		} else {
			sb.append(String.format("[%s] Data DB update Failed. baseDate: %s\n", entity.getTableName(), entity.getBaseDate()));
		}
		return false;
	}
	
	// 초단기예보 데이터 삭제
	private boolean deleteUltraSrtFcst(String baseDate) {
		UltraSrtFcstDAO dao = (UltraSrtFcstDAO) BeanUtils.getBean(UltraSrtFcstDAOImpl.class);
		
		UltraSrtFcst entity =  new UltraSrtFcst(baseDate, "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
		int updatedRow = dao.delete(entity);
		
		if (updatedRow>-1) {
			sb.append(String.format("[%s] Data prior to base date has been successfully deleted. baseDate: %s, updatedRow: %d\n", entity.getTableName(), baseDate, updatedRow));
			return true;
		} else {
			sb.append(String.format("[%s] Data DB update Failed. baseDate: %s\n", entity.getTableName(), entity.getFcstDate()));
		}
		return false;
	}
	
	// 동네예보 조회 데이터 삭제
	private boolean deleteVilageFcst(String baseDate) {
		VilageFcstDAO dao = (VilageFcstDAO) BeanUtils.getBean(VilageFcstDAOImpl.class);
		
		VilageFcst entity = new VilageFcst(baseDate, "", 0, 0, 0, 0, (float)0, 0, (float)0, 0, 0, (float)0, (float)0, 0, 0, (float)0, 0, 0);
		int updatedRow = dao.delete(entity);
		
		if (updatedRow>-1) {
			sb.append(String.format("[%s] Data prior to base date has been successfully deleted. baseDate: %s, updatedRow: %d\n", entity.getTableName(), baseDate, updatedRow));
			return true;
		} else {
			sb.append(String.format("[%s] Data DB update Failed. baseDate: %s\n", entity.getTableName(), entity.getFcstDate()));
		}
		return false;
	}
}
