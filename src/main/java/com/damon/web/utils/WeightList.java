package com.damon.web.utils;

import java.util.ArrayList;

public class WeightList extends ArrayList<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8204488058022062977L;

	public WeightList(String weights) {

		String[] ws = weights.split(",");

		for (String weight : ws) {

			super.add(Integer.parseInt(weight));
		}

	}

}
