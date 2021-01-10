package com.devachip.evaweather.persistence;

import com.devachip.evaweather.domain.VilageFcst;

public interface VilageFcstDAO {
	public int update(VilageFcst entity);
	public int insert(VilageFcst entity);
}
