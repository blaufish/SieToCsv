.PHONY: all
SHELL=/bin/bash
SOURCES=$(shell find src -type f -name '*.java')
TARGETJAR=output/jars/sie-to-csv-$(shell git describe).jar
all:
	@if [[ -n $$(git status -s) ]]; then echo "Error, Repository is not clean yet."; git status; exit 1; fi
	rm -rf output/bin
	mkdir -p output/bin
	mkdir -p output/jars
	javac -d output/bin $(SOURCES)
	jar cfm \
 $(TARGETJAR) \
 manifest.txt \
 -C output/bin org/blaufish/sie/SieToCsv.class \
 -C output/bin org/blaufish/sie/SieParser.class
	java -jar $(TARGETJAR)
