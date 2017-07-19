<%@ include file="/common/taglibs.jsp"%>

<html>
    <head>
        <title>About ASTAM Correlation</title>
    </head>
    <body>
        <h2>About ASTAM Correlation</h2>
        <div>
            <p>
                ASTAM Correlation is a Hybrid Analysis Mapping tool used to parse source code and merge scans from SAST and DAST tools to calculate attack surfaces for applications.
            </p>
            <p>
                <a href="http://astam.securedecisions.com/display/PUB/ASTAM" target="_blank">ASTAM Overview Site</a>
            </p>
        </div>
        <h2>About This ASTAM Correlation Build</h2>
        <div>
            <b>Build date:</b> <fmt:formatDate value="${requestScope.buildDate}" pattern="MMM dd, yyyy hh:mm a zzz"/>
        </div>
    </body>
</html>
