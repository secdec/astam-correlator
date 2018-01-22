
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

## Installation

Both Linux and Windows hosts are supported. Installation mostly follows the original ThreadFix install guide.

ThreadFix wiki: https://github.com/denimgroup/threadfix/wiki

Installation guides:
- Windows: https://github.com/denimgroup/threadfix/wiki/Windows-2012-R2-Installation-Guide
- Ubuntu: https://github.com/denimgroup/threadfix/wiki/Ubuntu-and-Debian-Installation-Guide

#### Differences from ThreadFix Install Guide

Follow all steps in either guide linked above. These differences are in comparison to the Ubuntu
installation guide. Ubuntu 16.04 LTS wasa used during installation.

1. Ignore the `Setup init script` section under `Install and Configure Tomcat 7`.

Instead, create a systemd service file: `sudo nano /etc/systemd/system/tomcat.service`

Enter the following contents into the service file and save:

    [Unit]
    Description=Apache Tomcat Web Application Container
    After=network.target
    
    [Service]
    Type=forking
    
    Environment=JAVA_HOME=/usr/lib/jvm/java-8-oracle/jre
    Environment=CATALINA_PID=/opt/tomcat/temp/tomcat.pid
    Environment=CATALINA_HOME=/opt/tomcat
    Environment=CATALINA_BASE=/opt/tomcat
    Environment='CATALINA_OPTS=-Xms512M -Xmx1024M -server -XX:+UseParallelGC'
    Environment='JAVA_OPTS=-Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom -Xms128m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=256m'
    
    WorkingDirectory=/opt/tomcat/temp
    ExecStart=/opt/tomcat/bin/startup.sh
    ExecStop=/opt/tomcat/bin/shutdown.sh
    
    User=tomcat
    Group=tomcat
    UMask=0007
    RestartSec=10
    Restart=always
    
    [Install]
    WantedBy=multi-user.target
    
2. After extracting Tomcat, the tomcat user must be given access and ownership over the `/opt/tomcat` directory.

        sudo chown -R tomcat:tomcat /opt/tomcat
        sudo chgrp -R tomcat /opt/tomcat

2. Before the step _Installing ThreadFix_, create a scratch directory for the ASTAM Correlator:

        sudo mkdir /etc/threadfix
        sudo mkdir /etc/threadfix/scratch
        sudo chown -R tomcat:tomcat /etc/threadfix
        sudo chgrp -R tomcat /etc/threadfix

3. The first step of the _Installing ThreadFix_ section should be ignored.
Instead of unzipping a ThreadFix zip file, copy the provided WAR file into `/opt/tomcat/webapps`. Rename the WAR file
 to 'threadfix', or whatever is appropriate. This name will determine the endpoint that ASTAM Correlator
 will be available at. Follow the _Update Permissions_ step and start `Tomcat` so that the WAR is
 extracted. Stop `Tomcat` once extracted. The remaining steps are the same.

4. The section _ThreadFix init Script_ can be ignored.

5. *Before running ThreadFix* and after completing the `Database Configuration` step:

Modify the file `/opt/tomcat/threadfix*/WEB-INF/classes/jdbc.properties` and change the parameter
`hibernate.hbm2ddl.auto=update`. Its value must be changed to `create` upon the first run. Start Tomcat
 and wait for the Correlator to start. You can check its progress using `tail /opt/tomcat/logs/astam.log -f`.
 This file will not exist until the Correlator has begin startup. It may take a few moments before
 the log file appears.
 
 The first run of the Correlator may take up to 30 minutes to finish startup depending on the machine
 running it.
 
 Initialization has finished once you see the message `Application version set to: ...`
 
 *Change the `hibernate.hbm2ddl.auto` value back to `update` once the ASTAM Correlator has completed startup the first time.* Restart
tomcat.

## Usage

The ASTAM Correlator presents a web-based interface.

##### After installation and starting Tomcat
Open your browser of choice and navigate to `http://localhost:8080/threadfix`
(port may change depending on your configuration). Your browser will display a a warning screen
about the SSL certificate. ThreadFix generates a unique self-signed SSL certificate when it first
starts up. You need to add the certificate to your list of trusted certificates to continue.

You will be presented with a sign-in page. The credentials for the built-in admin account are `user` and
`password`. These should be changed immediately.

##### Configure a Team

After signing in you will be asked to add a Team. Before adding an application for scanning, we
 must create a team that owns the application. Enter the name of your team and click "Add Team".

##### Configure an Application

Click _Add Application_ on the team directory to assign an application to your team. This will display
a popup with various fields to enter. The required fields are:

`Name` - Name of the application to be displayed in the web interface

`Team` - The team assigned to this application

`Criticality` - The importance of an application compared to other applications registered with the correlator, used for organization by the user

`Application Type` - The web framework used by the application to be added. These options are:
- `None` - Unrecognized framework, scan merging will not be performed
- `Detect` - Attempt to auto-detect the framework type of the application
- `JSP`
- `RAILS`
- `SPRING_MVC`
- `STRUTS`
- `DOT_NET_MVC`
- `DOT_NET_WEB_FORMS`
- `PYTHON` (Django framework)

`Source Code Information` - Click this link to display more options. *This must be filled out in order to correlate static and dynamic scans*
- `Source Code Repository Type` - Specify the SCM type of your application
- `Source Code URL` - A link to the remote repository containing your source code. This will automatically be pulled when a scan is uploaded
- `Source Code Branch` - The branch of the remote repository to pull from
- `Source Code Revision` - The ID of the commit to use when pulling from the given repository. If blank, the latest commit will always be used
- `Source Code User Name` - The username to use when authenticating against the remote repository
- `Source Code Password` - The password to use when authenticating against the remote repository
- `Source Code Folder` - The path to the copy of the source code, on the server running ThreadFix. Repository information can be ignored if a copy is available on the ThreadFix server and this path is defined

`Disable Vulnerability Merging` - Prevents the ASTAM Correlator from merging SAST and DAST scans for the given project

These settings can be changed after creating your application by navigating to the application's overview page by
clicking the `Action` drop-down and selecting `Edit / Delete`.

Once an application has been added, it should appear on the Teams page listed under the team you
created in the previous step.

##### Upload Scans

Click on an application in the Teams page to view the application's overview page. Click the `Action` drop-down
and select `Upload Scan`. A file picker dialog will appear. Select your SAST or DAST scan associated with the
given application.

The supported scan formats match those of the original ThreadFix software:
- https://github.com/denimgroup/threadfix/wiki/Static-Scanners
- https://github.com/denimgroup/threadfix/wiki/Dynamic-Scanners

Click OK, and the webpage will display a loading dialog. Once imported, the page will update and display
all of the imported vulnerabilities, organized by type and severity.

These entries can be expanded to display the available information regarding the vulnerabilities. This may include
the vulnerable URL, source code file, and query parameters. Scan findings that have been merged will
display both a URL and a path to the source code. Unmerged scans will display only one or the other.

Any number of scans may be uploaded for the same application. The ASTAM Correlator will automatically merge
duplicate vulnerabilities and combine their available information to provide a more detailed view into the
vulnerability.

## Shared Features

Being a derivative of Denim Groups' ThreadFix software, various features have carried over. These
include:

*Remote Providers* - 
=======
# ASTAM Correlator

The ASTAM Correlator is a vulnerability consolidation and management tool for web applications, capable of correlating
and merging different Static and Dynamic scans indicating the same vulnerability. This improves
scan results by combining findings that are symptoms of the same weakness, providing:

- More information on a vulnerability as a whole
- Reduced duplicate vulnerabilities from multiple SAST/DAST scans


# Supported Web Frameworks
The following frameworks are supported by the Correlator route detection process:

- ASP.NET MVC
- ASP.NET Web Forms
- Struts
- Django
- Ruby on Rails
- Spring MVC
- JSP

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
