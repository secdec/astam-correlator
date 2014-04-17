<script type="text/ng-template" id="newApplicationModal.html">
    <div class="modal-header">
        <h4 id="myModalLabel">
            New Application
        </h4>
    </div>

    <form id="newApplicationForm" name='form'>
        <div class="modal-body input-group">

            <table class="modal-form-table">
                <tr class="left-align">
                    <td>Name</td>
                    <td>
                        <input focus-on="focusInput" type='text' name='name' ng-model="object.name" required/>
                        <span class="errors" ng-show="form.name.$dirty && form.name.$error.required">Name is required.</span>
                    </td>
                </tr>
                <tr class="left-align">
                    <td>URL</td>
                    <td>
                        <input type='url' name='url' ng-model="object.url"/>
                        <span class="errors" ng-show="form.url.$dirty && form.url.$error.maxlength">Maximum length is 200.</span>
                    </td>
                </tr>
                <tr class="left-align">
                    <td>Unique ID</td>
                    <td>
                        <input name="uniqueId" type='text' style="margin-bottom:0px;"
                               ng-model="object.uniqueId"
                               id="uniqueIdInput{{ object.team.id }}" size="50" maxlength="255"/>
                    </td>
                </tr>
                <tr class="left-align">
                    <td>Team</td>
                    <td>{{ object.team.name }}</td>
                </tr>
                <tr class="left-align">
                    <td>Criticality</td>
                    <td>
                        <select name="applicationCriticality.id"
                                style="margin-bottom:0px;"
                                ng-model="object.applicationCriticality.id"
                                id="criticalityId${organization.id}">

                            <c:forEach items="${applicationCriticalityList}" var="applicationCriticality">
                                <option value="<c:out value='${applicationCriticality.id}'/>">
                                    <c:out value='${applicationCriticality.name}'/>
                                </option>
                            </c:forEach>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td class="right-align">Application Type</td>
                    <td class="left-align" >
                        <select name="frameworkType" ng-model="object.frameworkType" id="frameworkTypeSelect{{ object.team.id }}">
                            <c:forEach items="${applicationTypes}" var="type">
                                <option value="<c:out value='${type.displayName}'/>">
                                    <c:out value='${type.displayName}'/>
                                </option>
                            </c:forEach>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td class="right-align">Source Code URL</td>
                    <td class="left-align" >
                        <input name="repositoryUrl"
                                type='url' id="repositoryUrl{{ object.team.id }}"
                                maxlength="250" ng-model="object.repositoryUrl"/>
                    </td>
                </tr>
                <tr>
                    <td>Source Code Revision</td>
                    <td>
                        <input type="text" id="repositoryBranch" ng-model="object.repositoryBranch" maxlength="250" name="repositoryBranch"/>
                    </td>
                </tr>
                <tr>
                    <td>Source Code UserName</td>
                    <td>
                        <input type="text" id="repositoryUsername" ng-model="object.repositoryUserName" maxlength="250" name="repositoryUserName"/>
                    </td>
                </tr>
                <tr>
                    <td>Source Code Password</td>
                    <td>
                        <input type="password" id="repositoryPassword" ng-model="object.repositoryPassword" showPassword="true" maxlength="250" path="repositoryPassword"/>
                    </td>
                </tr>
                <tr>
                    <td class="right-align">Source Code Folder</td>
                    <td class="left-align" >
                        <input name="repositoryFolder"
                                type='text' id="repositoryFolder{{ object.team.id }}"
                                maxlength="250" ng-model="object.repositoryFolder"/>
                    </td>
                </tr>
            </table>

        </div>
        <%@ include file="/WEB-INF/views/modal/footer.jspf" %>
    </form>
</script>
