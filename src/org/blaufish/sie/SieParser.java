package org.blaufish.sie;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

class SieParser {
	Map<Integer, Map<Integer, Double>> monthAccountAmountMap;

	void parseSei(String filename) throws Exception {
		try (Stream<String> stream = Files.lines(Paths.get(filename), StandardCharsets.ISO_8859_1)) {
			stream.forEach(line -> {
				do {
					if (!line.startsWith("#PSALDO"))
						break;
					if (line.length() > 100) {
						warn(filename, "Ingoring suspicious long line.");
						break;
					}
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

	private Set<String> emittedWarnings = new HashSet<>();

	private void warn(String filename, String warningFmt, Object... warningExtraArgs) {
		StringBuilder sb = new StringBuilder();
		sb.append(filename).append(":").append(String.format(warningFmt, warningExtraArgs));
		String warning = sb.toString();
		if (emittedWarnings.add(warning))
			System.err.println(warning);
	}

	private void put(Integer month, Integer account, Double amount) {
		Map<Integer, Double> accountAmountMap = monthAccountAmountMap.get(month);
		if (accountAmountMap == null) {
			accountAmountMap = new TreeMap<>();
			monthAccountAmountMap.put(month, accountAmountMap);
		}
		accountAmountMap.put(account, amount);
	}
}
