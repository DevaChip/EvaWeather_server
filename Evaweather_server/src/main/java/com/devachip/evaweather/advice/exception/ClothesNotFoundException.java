package com.devachip.evaweather.advice.exception;

public class ClothesNotFoundException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClothesNotFoundException(String msg, Throwable t) {
		super(msg, t);
	}
	
	public ClothesNotFoundException(String msg) {
		super(msg);
	}
	
	public ClothesNotFoundException() {
		super();
	}
}
