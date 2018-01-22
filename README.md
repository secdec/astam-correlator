
_*This material is based on research sponsored by the Department of Homeland
Security (DHS) Science and Technology Directorate, Cyber Security Division
(DHS S&T/CSD) via contract number HHSP233201600058C.*_

# ASTAM Correlator

The ASTAM Correlator is a vulnerability navigation and consolidation tool capable of correlating
and merging different Static and Dynamic scans indicating the same vulnerability. This improves
scan results by combining findings that are symptoms of the same weakness, providing more
information on the vulnerability as a whole.

This project is a modification of Denim Group's software ThreadFix, which provides the Hybrid
Analysis Mapping (HAM) that runs the correlation. A collaboration between Denim Group and Secure
Decisions, a subdivision of Applied Visions, Inc., has improved upon the open-source ThreadFix tool
with a focused interface and improved HAM capabilities.

The original ThreadFix project can be found here: https://github.com/denimgroup/threadfix

---

## Disclaimer

The ASTAM Correlator is originally designed for integration into various other ASTAM components. Due to
external factors, this integration is no longer in development and no longer supported. This is with respect to
the `threadfix-astam` module and any ActiveMQ or Protobuf dependencies.


## Requirements

The ASTAM Correlator may be built from source or installed using the provided WAR file. It has the following
dependencies:

- Java 8
- Tomcat 7.0.75
- MySQL Server
- Linux- or Windows-based host

# Documentation

Instructions for the usage and installation of the ASTAM Correlator can be found in this project's [Wiki](https://github.com/secdec/astam-correlator/wiki).

# Contributors

This project is a modification of Denim Group's software ThreadFix, Community Edition, which provides the [Hybrid Analysis Mapping (HAM)](https://github.com/denimgroup/threadfix/wiki/HAM-Merging-Process-Explained) that runs the correlation. A collaboration between Denim Group Ltd., and Secure
Decisions, a subdivision of Applied Visions Inc., has improved upon the open-source ThreadFix tool
with a focused interface and improved HAM capabilities.

The original ThreadFix project can be found here: https://github.com/denimgroup/threadfix

-----

_*This material is based on research sponsored by the Department of Homeland
Security (DHS) Science and Technology Directorate, Cyber Security Division
(DHS S&T/CSD) via contract number HHSP233201600058C.*_
