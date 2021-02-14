package com.devachip.evaweather.advice;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.devachip.evaweather.advice.exception.ClothesNotFoundException;
import com.devachip.evaweather.dto.clothesapi.Clothes;
import com.devachip.evaweather.dto.clothesapi.ClothesResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class CRestControllerAdvice {
	
	@ExceptionHandler(ClothesNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String HandleClothesNotFoundException(ClothesNotFoundException e) throws JsonProcessingException {
		log.error("ClothesNotFoundException! cause by: {}", e.getMessage());
		
		return Optional.of(new Clothes(null, "no data"))
				.map(clothes -> new ClothesResponse(Arrays.asList(new Clothes[] {clothes})))
				.map(r -> {
					try {
						return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(r);
					} catch (JsonProcessingException e1) {
						log.error(e1.fillInStackTrace() + "");
					}
					
					return "{\"error\" : \"Failed to jsonResponse\"}";
				})
				.orElseThrow();
	}
}
