package org.blaufish.sie;

import java.util.Map.Entry;
import java.util.Set;
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
		System.out.println();
		displayCsv3();
	}

	private static TreeMap<Integer, Double> financialResult() {
		TreeMap<Integer, Double> map = new TreeMap<>();
		for (Entry<Integer, TreeMap<Integer, Double>> e : parser.monthAccountAmountMap.entrySet()) {
			Integer month = e.getKey();
			Double amount = 0.0;
			for (Entry<Integer, Double> accountAmount : e.getValue().entrySet()) {
				Integer account = accountAmount.getKey();
				if (accountAffectResult(account))
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
				if (accountIsEarning(account))
					amount -= accountAmount.getValue();
			}
			map.put(month, amount);
		}
		return map;
	}

	private static TreeSet<Integer> financialCostsSet() {
		TreeSet<Integer> set = new TreeSet<>();
		for (Entry<Integer, TreeMap<Integer, Double>> e : parser.monthAccountAmountMap.entrySet()) {
			for (Entry<Integer, Double> accountAmount : e.getValue().entrySet()) {
				Integer account = accountAmount.getKey();
				if (accountIsCost(account))
					set.add(account);
			}
		}
		return set;
	}

	private static TreeMap<String, TreeMap<Integer, Double>> financialCostMonthAmount() {
		TreeSet<Integer> set = financialCostsSet();
		Set<Integer> months = parser.monthAccountAmountMap.keySet();
		Integer id = null;
		String desc = null;
		TreeMap<String, TreeMap<Integer, Double>> kontoDescriptionsMonthAmountMap = new TreeMap<>();
		TreeMap<Integer, Double> monthAmountMap = null;
		for (Entry<Integer, String> kontoDescriptionEntry : parser.kontoDescriptionMap.entrySet()) {
			Integer kontoCode = kontoDescriptionEntry.getKey();
			if (!set.contains(kontoCode))
				continue;
			String line = String.format("(%d) %s", kontoCode, kontoDescriptionEntry.getValue());
			if (id == null) {
				id = kontoCode;
				desc = line;
				monthAmountMap = new TreeMap<>();
			} else {
				if (id.intValue() / 1000 != kontoCode.intValue() / 1000) {
					kontoDescriptionsMonthAmountMap.put(desc, monthAmountMap);
					id = kontoCode;
					desc = line;
					monthAmountMap = new TreeMap<>();
				} else {
					desc = String.format("%s, %s", desc, line);
				}
			}
			for (Integer month : months) {
				TreeMap<Integer, Double> accountAmountMap = parser.monthAccountAmountMap.get(month);
				Double amount = accountAmountMap.get(kontoCode);
				if (amount == null)
					amount = 0.0d;
				Double oldAmount = monthAmountMap.get(month);
				if (oldAmount == null)
					oldAmount = 0.0d;
				monthAmountMap.put(month, amount.doubleValue() + oldAmount.doubleValue());
			}
		}
		kontoDescriptionsMonthAmountMap.put(desc, monthAmountMap);
		return kontoDescriptionsMonthAmountMap;
	}

	private static boolean accountAffectResult(Integer account) {
		return accountIsEarning(account) || accountIsCost(account);
	}

	private static boolean accountIsEarning(Integer account) {
		if (account < 3000)
			return false;
		if (account > 3999)
			return false;
		return true;
	}

	private static boolean accountIsCost(Integer account) {
		if (account < 4000)
			return false;
		if (account > 7999)
			return false;
		return true;
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

	private static void displayCsv3() {
		TreeMap<String, TreeMap<Integer, Double>> kontoDescriptionsMonthAmountMap = financialCostMonthAmount();

		System.out.print(";");
		Set<Integer> months = parser.monthAccountAmountMap.keySet();
		for (Integer month : months) {
			System.out.printf("%d;", month);
		}
		System.out.println();
		for (Entry<String, TreeMap<Integer, Double>> entry : kontoDescriptionsMonthAmountMap.entrySet()) {
			String line = entry.getKey();
			System.out.printf("%s;", line);
			TreeMap<Integer, Double> map = entry.getValue();
			for (Double value : map.values()) {
				System.out.printf("%.0f;", value);
			}
			System.out.println();
		}
	}

}
