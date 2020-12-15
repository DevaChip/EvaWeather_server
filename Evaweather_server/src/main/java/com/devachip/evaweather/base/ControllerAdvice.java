package com.devachip.evaweather.base;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * REST API 컨트롤러에서 발생한 에러 처리 담당
 * 
 * @author dykim
 * @since 2020.12.15
 */
@RestControllerAdvice
public class ControllerAdvice {

	@ExceptionHandler({ MissingServletRequestParameterException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String BadRequestExceptionHandler(Exception ex) {
		return String.format("{\"message\":\"%s\"}", ex.getMessage());
	}

	@ExceptionHandler({ NoHandlerFoundException.class })
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String NoHandlerFoundExceptionHandler(Exception ex) {
		return String.format("{\"message\":\"%s\"}", ex.getMessage());
	}
}
