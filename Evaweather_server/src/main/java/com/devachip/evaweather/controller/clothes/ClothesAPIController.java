package com.devachip.evaweather.controller.clothes;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devachip.evaweather.service.clothes.ClothesService;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/clothes")
public class ClothesAPIController {

	private ClothesService service;

	public ClothesAPIController(ClothesService service) {
		this.service = service;
	}

	@ApiOperation(value="get Clothes", notes="성별에 맞는 옷 정보 조회")
	@GetMapping("/get")
	public String getClothes(@RequestParam String gender) {
		return service.getClothes(gender);
	}
}
