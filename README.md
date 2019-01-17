# SIE to CSV #

Parses SIE files for **\#PSALDO** tag and produce earnings, balance reports in CSV format. It applies some logic to determine what is earnings, etc.
This is an extremly basic tool that ignores virtually all other tags, sufficient for some small companies, no guarantees it does anything useful to you.

Usage:

```
java -jar output/jars/sie-to-csv-v1.jar 2017.sie 2018.sie
```

Emits CSV file with semicolon separator. Easily pasted into e.g. Excel for building graphs.

# License #

This project is [licensed](LICENSE.md) under MIT License;
"A short and simple permissive license with conditions only
requiring preservation of copyright and license notices. Licensed
works, modifications, and larger works may be distributed under
different terms and without source code."

# SIE Fornat #

The SIE format is an open standard for transferring accounting data between different software produced by different software suppliers.

SIE format is managed by SIE Gruppen [www.sie.se](http://www.sie.se/), no affiliation.
