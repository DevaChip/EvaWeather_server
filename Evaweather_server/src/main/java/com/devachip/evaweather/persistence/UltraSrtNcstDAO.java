package com.devachip.evaweather.persistence;

import com.devachip.evaweather.domain.UltraSrtNcst;

public interface UltraSrtNcstDAO {
	public int update(UltraSrtNcst entity);
	public int insert(UltraSrtNcst entity);
	public int delete(UltraSrtNcst entity);
}
