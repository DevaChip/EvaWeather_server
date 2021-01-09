package com.devachip.evaweather.service;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.devachip.evaweather.bean.DBConnect;

@Service
public class ApplicationService implements ApplicationListener<ContextClosedEvent> {
	private DBConnect dbConnect;
	
	public ApplicationService(DBConnect dbConnect) {
		this.dbConnect = dbConnect;
	}
	
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {	// 서버 종료 시 실행됨.
		 shutdownScheduler();	// 스케쥴러 종료
		
		 DBConnect.close(dbConnect.getConnection());	// DB 연결 종료
	}
	
	private void shutdownScheduler() {
		try {
			WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
			Scheduler scheduler = (Scheduler) context.getBean("schedulerFactory");
			scheduler.shutdown(true);
			
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

}