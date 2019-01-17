package org.blaufish.sie;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

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
		System.out.println();
		displayCsv2();
	}

	private static TreeMap<Integer, Double> financialResult() {
		TreeMap<Integer, Double> map = new TreeMap<>();
		for (Entry<Integer, TreeMap<Integer, Double>> e : parser.monthAccountAmountMap.entrySet()) {
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

	private static TreeMap<Integer, Double> financialEarnings() {
		TreeMap<Integer, Double> map = new TreeMap<>();
		for (Entry<Integer, TreeMap<Integer, Double>> e : parser.monthAccountAmountMap.entrySet()) {
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
		TreeMap<Integer, Double> result = financialResult();
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
		TreeMap<Integer, Double> earnings = financialEarnings();
		System.out.printf("%s;", "Omsättning");
		for (Double amount : earnings.values()) {
			System.out.printf("%.0f;", amount);
		}
		System.out.println();
	}

	private static void displayCsv2() {
		TreeMap<Integer, Double> earnings = financialEarnings();
		TreeSet<Integer> years = new TreeSet<>();
		for (int yearmonth : earnings.keySet())
			years.add(yearmonth / 100);
		System.out.print(";");
		for (int i = 1; i < 13; i++)
			System.out.printf("%d;", i);
		System.out.println();
		for (int year : years) {
			System.out.printf("%d;", year);
			for (int i = 1; i < 13; i++) {
				Double amount = earnings.get(year * 100 + i);
				if (amount == null)
					amount = 0.0d;
				System.out.printf("%.0f;", amount);
			}
			System.out.println();
		}
	}
}
