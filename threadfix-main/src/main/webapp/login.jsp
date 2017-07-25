<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="/common/taglibs.jsp"%>

<html lang="en">
<head>
	<meta http-equiv="X-FRAME-OPTIONS" content="DENY"/>
    <%@include file="/common/meta.jsp"%>
	<title>ASTAM Correlator</title>
    <cbs:cachebustscript src="/scripts/angular.min.js"/>
    <cbs:cachebustscript src="/scripts/angular-sanitize.min.js"/>
    <cbs:cachebustscript src="/scripts/login-controller.js"/>
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/login.css"/>
</head>

<body ng-app="threadfix">
	<spring:url value="login" var="loginUrl"/>
	<div style="position:absolute;left:50%;top:50%;margin-top:-100px;margin-left:-250px;width:500px;height:220px">
        <table style="width:500px;height:200px;border-width:1px;border-collapse:collapse;border-color:black;border-style:solid;">
            <tr style="height:200px;background:#EFEFEF;">
                <td>
                    <img src="<%=request.getContextPath()%>/images/ASTAM_logo.png" alt="ASTAM" style="margin-top: -90px"/>
                </td>
            </tr>
        </table>
	</div>

    <div ng-controller="LoginController">
        <form method="post" action="${ fn:escapeXml(loginUrl) }" autocomplete="off" name="form">
            <!-- Attempts to change this will only result in the CSRF filter blocking the user -->
            <input type='hidden' name='spring-security-redirect' value='/teams'/>
            <c:if test="${param.sessionTimeout =='true'}">
                <div id="loginError" class="sessionTimeout" style="position:absolute;left:50%;top:50%;margin-left:-250px;margin-top:-68px;width:500px;text-align:center;color:red;font-weight:bold">
                    This session has been expired due to inactivity.
                </div>
            </c:if>
            <c:if test="${param.concurrentSessions =='true'}">
                <div id="loginError" class="concurrentSessions" style="position:absolute;left:50%;top:50%;margin-left:-250px;margin-top:-68px;width:500px;text-align:center;color:red;font-weight:bold">
                    This session has been expired (possibly due to multiple concurrent logins being attempted as the same user).
                </div>
            </c:if>
            <c:if test="${SPRING_SECURITY_LAST_EXCEPTION.message =='Bad credentials'}">
                <div id="loginError" style="position:absolute;left:50%;top:50%;margin-left:-250px;margin-top:-68px;width:500px;text-align:center;color:red;font-weight:bold">
                    Error: Username or Password incorrect
                </div>
            </c:if>
            <c:if test="${SPRING_SECURITY_LAST_EXCEPTION.message =='Check logs'}">
                <div id="loginError" style="position:absolute;left:50%;top:50%;margin-left:-250px;margin-top:-68px;width:500px;text-align:center;color:red;font-weight:bold">
                    Error: Your database may have been created incorrectly.<br>Please check logs for more information.
                </div>
            </c:if>
            <div style="position:absolute;left:50%;top:50%;margin-left:-143px;margin-top:-32px;color:black;width:70px;text-align:right;">
                Username
            </div>
            <div style="position:absolute;left:50%;top:50%;margin-left:-63px; margin-top:-32px;">
                <input type="text" style="width:200px" id="username" class="textbox focus" name="username" required/>
                {{ test }}
            </div>
            <div style="position:absolute;left:50%;top:50%;margin-left:-143px;margin-top:9px;color:black;width:70px;text-align:right;">
                Password
            </div>
            <div style="position:absolute;left:50%;top:50%;margin-left:-63px; margin-top:9px;">
                <input type="password" style="width:200px" class="textbox" id="password" name="password" required/>
            </div>
            <div style="position:absolute;left:50%;top:50%;margin-left:-65px; margin-top:51px;">
                <button ng-class="{ disabled : form.$invalid }" id="login" style="width:130px;">Login</button>
            </div>
        </form>
    </div>
</body>
</html>
