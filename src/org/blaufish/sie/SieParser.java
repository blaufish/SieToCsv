package org.blaufish.sie;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class SieParser {
	TreeMap<Integer, TreeMap<Integer, Double>> monthAccountAmountMap = new TreeMap<>();
	TreeMap<Integer, String> kontoDescriptionMap = new TreeMap<>();
	private Charset cp437;
	private Pattern kontoPattern;

	void parseSei(String filename) throws Exception {
		setCp437Charset();
		setKontoPattern();
		try (Stream<String> stream = Files.lines(Paths.get(filename), cp437)) {
			stream.forEach(line -> {
				do {
					if (line.length() > 150) {
						warn(filename, "Ingoring suspicious long line.");
						continue;
					}
					/* 5.8 Multiple space and tab accepted as space separator */
					line = line.replaceAll("\\s+", " ");
					parsePsaldo(filename, line);
					parseKonto(filename, line);
				} while (false);
			});
		}
	}

	private void parsePsaldo(String filename, String line) {
		if (!line.startsWith("#PSALDO"))
			return;
		String[] splitted = line.split("[{}]");
		if (splitted.length != 3) {
			warn(filename, "Ingoring suspicious malformed line.");
			return;
		}
		String[] leftWords = splitted[0].split(" ");
		String zero = leftWords[1];
		if (!zero.equals("0"))
			return;
		String month = leftWords[2];
		String account = leftWords[3];
		String dimensionInformation = splitted[1].trim();
		if (dimensionInformation.length() != 0) {
			warn(filename, "%s (ignoring dimension information)", dimensionInformation);
			return;
		}
		String[] rightWords = splitted[2].trim().split(" ");
		String amount = rightWords[0];
		String secondZero = rightWords[1];
		if (!secondZero.equals("0")) {
			warn(filename, "%s (don't know how to parse yet)", line);
			return;
		}
		Integer imonth = Integer.valueOf(month);
		Integer iaccount = Integer.valueOf(account);
		Double damount = Double.valueOf(amount);
		//if (iaccount >= 3000 && iaccount <= 5000)
		//	System.out.printf("month:%d account:%d amount:%f line:%s\n", imonth, iaccount, damount, line);
		put(imonth, iaccount, damount);
	}

	private void parseKonto(String filename, String line) {
		if (!line.startsWith("#KONTO"))
			return;
		Matcher matcher = kontoPattern.matcher(line);
		if (!matcher.matches()) {
			warn(filename, "Parsing failed: " + line);
			return;
		}
		String kontoCode = matcher.group(1);
		String kontoDescription = matcher.group(2);
		kontoDescriptionMap.put(Integer.valueOf(kontoCode), kontoDescription);
	}

	private void setKontoPattern() {
		if (kontoPattern != null)
			return;
		kontoPattern = Pattern.compile("^#KONTO (\\d+) \"(.*)\"$");
	}

	/*
	 * 5.7 Teckenrepertoaren i filen ska vara IBM PC 8-bitars extended ASCII
	 * (Codepage 437)
	 */
	private void setCp437Charset() {
		if (cp437 != null)
			return;
		try {
			cp437 = Charset.forName("Cp437");
		} catch (UnsupportedCharsetException e) {
			warn("Cp437 unspported on current platform");
			// ISO_8859_1 shouldn't fail hard.
			cp437 = StandardCharsets.ISO_8859_1;
		}
	}

	private Set<String> emittedWarnings = new HashSet<>();

	private void warn(String filename, String warningFmt, Object... warningExtraArgs) {
		StringBuilder sb = new StringBuilder();
		sb.append(filename).append(":").append(String.format(warningFmt, warningExtraArgs));
		String warning = sb.toString();
		warn(warning);
	}

	private void warn(String warning) {
		if (emittedWarnings.add(warning))
			System.err.println(warning);
	}

	private void put(Integer month, Integer account, Double amount) {
		TreeMap<Integer, Double> accountAmountMap = monthAccountAmountMap.get(month);
		if (accountAmountMap == null) {
			accountAmountMap = new TreeMap<>();
			monthAccountAmountMap.put(month, accountAmountMap);
		}
		accountAmountMap.put(account, amount);
	}
}
