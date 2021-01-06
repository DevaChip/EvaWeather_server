package com.devachip.evaweather.util;

import org.springframework.context.ApplicationContext;

import com.devachip.evaweather.bean.ApplicationContextProvider;

public class BeanUtils {
	public static Object getBean(String beanName) {
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		return context.getBean(beanName);
	}
}
