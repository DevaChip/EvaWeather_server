package com.devachip.evaweather.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import com.devachip.evaweather.bean.ApplicationContextProvider;

public class BeanUtils {
	private static ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
	
	public static Object getBean(Object obj) {
		try {
			if (obj instanceof String) {
				return ctx.getBean((String) obj);
			}
			
			if (obj instanceof Class) {
				return ctx.getBean((Class<?>) obj);
			}
		} catch (BeansException e) {
			e.fillInStackTrace();
		} catch (ClassCastException e) {
			e.fillInStackTrace();
		} catch (Exception e) {
			e.fillInStackTrace();
		}
		
		return null;
	}
}
