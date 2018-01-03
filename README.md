# ASTAM Correlator

The ASTAM Correlator is a vulnerability navigation and consolidation tool capable of correlating
and merging different Static and Dynamic scans indicating the same vulnerability. This improves
scan results by combining findings that are symptoms of the same weakness, providing more
information on the vulnerability as a whole.

The ASTAM Correlator is a modification of the Denim Group software Threadfix, which provides the Hybrid
Analysis Mapping (HAM) that runs the correlation. A collaboration between Denim Group and Secure
Decisions, a subdivision of Applied Visions, Inc., has improved upon the open-source Threadfix tool
with a focused interface and improved HAM capabilities.



## Requirements

The ASTAM Correlator must be built from source. It has the following software dependencies:

- Java 8
- Tomcat 7.0.75
- MySQL Server
- Linux- or Windows-based host

## Installation

Both Linux and Windows hosts are supported. Installation mostly follows the original Threadfix install guide.

Threadfix wiki: https://github.com/denimgroup/threadfix/wiki

Installation guide:
- Windows: https://github.com/denimgroup/threadfix/wiki/Windows-2012-R2-Installation-Guide
- Ubuntu: https://github.com/denimgroup/threadfix/wiki/Ubuntu-and-Debian-Installation-Guide

#### Differences from Threadfix Install Guide

Follow the steps up until the section _Installing Threadfix_.

The ASTAM Correlator is provided directly as a WAR file. Copy the WAR file to `/opt/tomcat/webapps` to install
the ASTAM Correlator.

Additional configuration is required:




## Usage

