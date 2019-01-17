package org.blaufish.sei;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Stream;

public class SeiToCsv {

	public static void main(String[] args) throws Exception {
		if ((args == null) || (args.length == 0)) {
			System.out.println("args: sei-file");
			return;
		}
		reset();
		for (String arg : args)
			parseSei(arg);
		displayCsv();
	}

	static Map<Integer, Map<Integer, Double>> monthAccountAmountMap;

	private static void reset() {
		monthAccountAmountMap = new TreeMap<>();
	}

	private static void put(Integer month, Integer account, Double amount) {
		Map<Integer, Double> accountAmountMap = monthAccountAmountMap.get(month);
		if (accountAmountMap == null) {
			accountAmountMap = new TreeMap<>();
			monthAccountAmountMap.put(month, accountAmountMap);
		}
		accountAmountMap.put(account, amount);
	}

	private static Map<Integer, Double> financialResult() {
		Map<Integer, Double> map = new TreeMap<>();
		for (Entry<Integer, Map<Integer, Double>> e : monthAccountAmountMap.entrySet()) {
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
		for (Entry<Integer, Map<Integer, Double>> e : monthAccountAmountMap.entrySet()) {
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

	private static void parseSei(String filename) throws Exception {
		try (Stream<String> stream = Files.lines(Paths.get(filename), StandardCharsets.ISO_8859_1)) {
			stream.forEach(line -> {
				do {
					if (!line.startsWith("#PSALDO"))
						break;
					String[] words = line.split(" ");
					String zero = words[1];
					if (!zero.equals("0"))
						return;
					String month = words[2];
					String account = words[3];
					String braces = words[4];
					if (!braces.equals("{}")) {
						/*
						 * TODO implement support for dimensions, under dimensions
						 * #DIM 20 "Avdelning"
						 * #UNDERDIM 21 "Underavdelning" 20
						 * #OBJECT 21 "0101" "Spadbarn"
						 * #PSALDO 0 200801 4010 {21 "0101"} 13200.00
						 */
						System.err.printf("%s:%s (don't know how to parse yet)\n", filename, line);
						break;
					}
					String amount = words[5];
					String secondZero = words[6];
					if (!secondZero.equals("0")) {
						System.err.printf("%s:%s (don't know how to parse yet)\n", filename, line);
						break;
					}
					put(Integer.valueOf(month), Integer.valueOf(account), Double.valueOf(amount));
				} while (false);
			});
		}
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
