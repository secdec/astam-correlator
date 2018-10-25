# ASTAM Correlator

The ASTAM Correlator is a vulnerability consolidation and management tool for web applications, capable of correlating
and merging different Static and Dynamic scans indicating the same vulnerability. This improves
scan results by combining findings that are symptoms of the same weakness, providing:

- More information on a vulnerability as a whole
- Reduced duplicate vulnerabilities from multiple SAST/DAST scans


# Supported Web Frameworks
The following frameworks are supported by the Correlator route detection process:

- ASP.NET MVC / Web API / Core / Web Forms
- Struts
- Django
- Ruby on Rails
- Spring MVC
- JSP

# Referencing the Endpoint Detection HAM Module
The ASTAM Correlator HAM module for endpoint detection has been published to Maven. You can add it as a dependency by adding this to your `pom.xml`:

    <dependency>
        <groupId>com.github.secdec.astam-correlator</groupId>
        <artifactId>threadfix-entities</artifactId>
        <version>1.3.4</version>
    </dependency>
    <dependency>
        <groupId>com.github.secdec.astam-correlator</groupId>
        <artifactId>threadfix-ham</artifactId>
        <version>1.3.4</version>
    </dependency>

# Documentation

Instructions for the usage and installation of the ASTAM Correlator can be found in this project's [Wiki](https://github.com/secdec/astam-correlator/wiki).

# Contributors

This project is a modification of Denim Group's software ThreadFix, Community Edition, which provides the [Hybrid Analysis Mapping (HAM)](https://github.com/denimgroup/threadfix/wiki/HAM-Merging-Process-Explained) that runs the correlation. A collaboration between Denim Group Ltd., and Secure
Decisions, a division of Applied Visions Inc., has improved upon the open-source ThreadFix tool
with a focused interface and improved HAM capabilities.

The original ThreadFix project can be found here: https://github.com/denimgroup/threadfix

-----

_*This material is based on research sponsored by the Department of Homeland
Security (DHS) Science and Technology Directorate, Cyber Security Division
(DHS S&T/CSD) via contract number HHSP233201600058C.*_
