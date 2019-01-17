package org.blaufish.sie;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class SieToCsv {

	static SieParser parser;

	public static void main(String[] args) throws Exception {
		if ((args == null) || (args.length == 0)) {
			System.out.println("args: sei-file");
			return;
		}
		parser = new SieParser();
		for (String arg : args)
			parser.parseSei(arg);
		displayCsv();
	}

	private static Map<Integer, Double> financialResult() {
		Map<Integer, Double> map = new TreeMap<>();
		for (Entry<Integer, Map<Integer, Double>> e : parser.monthAccountAmountMap.entrySet()) {
			Integer month = e.getKey();
			Double amount = 0.0;
			for (Entry<Integer, Double> accountAmount : e.getValue().entrySet()) {
				Integer account = accountAmount.getKey();
				if (account < 3000)
					continue;
				if (account > 8999)
					continue;
				amount -= accountAmount.getValue();
			}
			map.put(month, amount);
		}
		return map;
	}

	private static Map<Integer, Double> financialEarnings() {
		Map<Integer, Double> map = new TreeMap<>();
		for (Entry<Integer, Map<Integer, Double>> e : parser.monthAccountAmountMap.entrySet()) {
			Integer month = e.getKey();
			Double amount = 0.0;
			for (Entry<Integer, Double> accountAmount : e.getValue().entrySet()) {
				Integer account = accountAmount.getKey();
				if (account < 3000)
					continue;
				if (account > 3999)
					continue;
				amount -= accountAmount.getValue();
			}
			map.put(month, amount);
		}
		return map;
	}

	private static void displayCsv() {
		Map<Integer, Double> result = financialResult();
		System.out.print(";");
		for (Integer month : result.keySet()) {
			System.out.printf("%d;", month);
		}
		System.out.println();
		System.out.printf("%s;", "Resultat");
		for (Double amount : result.values()) {
			System.out.printf("%.0f;", amount);
		}
		System.out.println();
		Map<Integer, Double> earnings = financialEarnings();
		System.out.printf("%s;", "Omsättning");
		for (Double amount : earnings.values()) {
			System.out.printf("%.0f;", amount);
		}
		System.out.println();
	}
}
