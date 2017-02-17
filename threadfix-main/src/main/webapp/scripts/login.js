<script type="text/javascript">
    if (getCookie("username") != null) {
        $("username").value = getCookie("username");
        $("password").focus();
    } else {
        $("username").focus();
    }
    
    function saveUsername(theForm) {
        var expires = new Date();
        expires.setTime(expires.getTime() + 24 * 30 * 60 * 60 * 1000); // sets it for approx 30 days.
        setCookie("username",theForm.username.value,expires,"<c:url value="/"/>");
    }
    
    function validateForm(form) {                                                               
        return validateRequired(form); 
    } 
    
    function passwordHint() {
        if ($("username").value.length == 0) {
            alert("<fmt:message key="errors.required"><fmt:param><fmt:message key="label.username"/></fmt:param></fmt:message>");
            $("username").focus();
        } else {
            location.href="<c:url value="/passwordHint.html"/>?username=" + $("username").value;
        }
    }
    
    function required () { 
        this.aa = new Array("username", "<fmt:message key="errors.required"><fmt:param><fmt:message key="label.username"/></fmt:param></fmt:message>", new Function ("varName", " return this[varName];"));
        this.ab = new Array("password", "<fmt:message key="errors.required"><fmt:param><fmt:message key="label.password"/></fmt:param></fmt:message>", new Function ("varName", " return this[varName];"));
    } 
</script>