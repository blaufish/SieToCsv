package org.blaufish.sie;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

class SieParser {
	TreeMap<Integer, TreeMap<Integer, Double>> monthAccountAmountMap = new TreeMap<>();
	private Charset cp437;

	void parseSei(String filename) throws Exception {
		setCp437Charset();
		try (Stream<String> stream = Files.lines(Paths.get(filename), cp437)) {
			stream.forEach(line -> {
				do {
					if (!line.startsWith("#PSALDO"))
						break;
					if (line.length() > 100) {
						warn(filename, "Ingoring suspicious long line.");
						break;
					}
					/* 5.8 Multiple space and tab accepted as space separator */
					line = line.replaceAll("\\s+", " ");
					String[] splitted = line.split("[{}]");
					if (splitted.length != 3) {
						warn(filename, "Ingoring suspicious malformed line.");
						break;
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
					}
					String[] rightWords = splitted[2].trim().split(" ");
					String amount = rightWords[0];
					String secondZero = rightWords[1];
					if (!secondZero.equals("0")) {
						warn(filename, "%s (don't know how to parse yet)", line);
						break;
					}
					put(Integer.valueOf(month), Integer.valueOf(account), Double.valueOf(amount));
				} while (false);
			});
		}
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
