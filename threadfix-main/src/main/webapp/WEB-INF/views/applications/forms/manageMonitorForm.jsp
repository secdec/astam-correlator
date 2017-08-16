<script type="text/ng-template" id="manageMonitorForm.html">
    <div class="modal-header">
        <h4>Monitor Configuration</h4>
    </div>
    <%@ include file="/WEB-INF/views/successMessage.jspf"%>
    <%@ include file="/WEB-INF/views/errorMessage.jspf"%>
    <div class="modal-body container" ng-form="form">
        <div id="monitor-config-div" class="row span8">
            <div class="row span6" >
                <span id="enableLabel" class="span2"><c:out value="Enabled"/></span>
                <input type="checkbox" id="monitorEnabled" class="span1" ng-model="monitor.enabled" type="checkbox" >
            </div>
            <div class="row span8">
                <span id="hourLabel" class="span2"><c:out value="Hour"/></span>
                <input id="hour" name="hour" value="{{monitor.hour}}" ng-model="monitor.hour"
                       class="span2" type="number" focus-on="focusInput" ng-maxlength="2" required="true"
                       ng-attr-max="{{(monitor.frequency != undefined && monitor.frequency !== 'RECURRING') ? '12' : '23'}}"
                       ng-attr-min="{{(monitor.frequency != undefined && monitor.frequency !== 'RECURRING') ? '1' : '0'}}" ng-disabled="!monitor.enabled">
                <span id="hourRequiredError" class="errors" ng-show="form.hour.$error.required">Hour is required.</span>
                <span ng-show="monitor.frequency != undefined && monitor.frequency !== 'RECURRING'">
                    <span id="hrMinRequiredError" class="errors" ng-show="form.hour.$dirty && form.hour.$error.min">Must be 1 or greater.</span>
                    <span id="hrMaxRequiredError" class="errors" ng-show="form.hour.$dirty && form.hour.$error.max">Must be 12 or less.</span>
                </span>
                <span ng-show="monitor.frequency != undefined && monitor.frequency === 'RECURRING'">
                    <span id="hrMinRecRequiredError" class="errors" ng-show="form.hour.$dirty && form.hour.$error.min" >Must be 0 or greater.</span>
                    <span id="hrMaxRecRequiredError" class="errors" ng-show="form.hour.$dirty && form.hour.$error.max">Must be 23 or less.</span>
                </span>
            </div>
            <div class="row span8">
                <span id="minuteLabel" class="span2"><c:out value="Minute"/></span>
                <input id="minute" name="minute" value="{{monitor.minute}}" ng-model="monitor.minute" class="span2"
                       type="number" required="true" min="0" max="59" ng-disabled="!monitor.enabled">
                <span id="minRequiredError" class="errors" ng-show="form.minute.$dirty && form.minute.$error.required">Minute is required.</span>
                <span id="minMinRequiredError" class="errors" ng-show="form.minute.$dirty && form.minute.$error.min">Must be 0 or greater.</span>
                <span id="minMaxRequiredError" class="errors" ng-show="form.minute.$dirty && form.minute.$error.max">Must be 59 or less.</span>
            </div>
            <div class="row span6">
                <span id="periodLabel" class="span2"><c:out value="Time of Day"/></span>
                <select ng-options="period for period in periodTypes"
                        ng-model="monitor.period"
                        id="monitorPeriod"
                        name="monitorPeriod"
                        class="span2"
                        ng-disabled="monitor.frequency === 'RECURRING' || !monitor.enabled">
                </select>
            </div>
            <div class="row span6">
                <span id="frequencyTypeLabel" class="span2"><c:out value="Frequency"/></span>
                <select ng-options="types for types in frequencyTypes"
                        ng-model="monitor.frequency"
                        id="frequency"
                        name="frequency" class="span2" ng-disabled="!monitor.enabled" required="true"/>
                <span id="freqRequiredError" class="errors" ng-show="form.frequency.$dirty && form.frequency.$error.required">Frequency is Required.</span>

            </div>
            <div class="row span6">
                <span id="daysLabel" class="span2"><c:out value="Day"/></span>
                <select ng-options="types for types in days"
                        ng-model="monitor.day"
                        id="monitorDays"
                        name="monitorDays" class="span2" ng-disabled="monitor.frequency !== 'WEEKLY' || !monitor.enabled"
                        ng-required="monitor.frequency === 'WEEKLY'"/>
            </div>
            <input type="hidden" ng-model="monitor.id" value="{{monitor.id}}" readonly="true"/>
        </div>
    </div>
    <div class="modal-footer">
        <a id="closeModalButton" class="btn" ng-click="cancel()">Close</a>
        <c:if test="${ canManageApplications }">
            <button id="saveMonitor"
                    class="btn btn-primary"
                    ng-click="saveMonitor()" ng-disabled="form.$invalid || form.$pristine">Save</button>
        </c:if>
    </div>
</script>

