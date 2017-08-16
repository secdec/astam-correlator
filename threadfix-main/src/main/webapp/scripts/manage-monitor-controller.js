// Copyright 2017 Secure Decisions, a division of Applied Visions, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// This material is based on research sponsored by the Department of Homeland
// Security (DHS) Science and Technology Directorate, Cyber Security Division
// (DHS S&T/CSD) via contract number HHSP233201600058C.

/**
 * Created by jrios on 6/9/2017.
 */
var myAppModule = angular.module('threadfix');

myAppModule.controller('ManageMonitorController', function ($modal, $log, $scope, $rootScope, $modalInstance, $http, object, config, tfEncoder,  threadFixModalService) {

    $scope.object = object;
    $scope.config = config;
    $scope.focusInput = true;
    //Should move these over from the server side
    $scope.frequencyTypes = config.frequencyTypes ? config.frequencyTypes : ["DAILY", "WEEKLY", "RECURRING"]
    $scope.periodTypes = config.periodTypes ? config.periodTypes : ["AM", "PM"]
    $scope.days = config.daysOfWeek ? config.daysOfWeek : ["Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"]

    $scope.monitor = config.monitor;
    if($scope.monitor == undefined) {
        $scope.monitor = {}
    }

    $scope.saveMonitor = function(){

        var saveUrl = tfEncoder.encode("/organizations/" + $scope.config.application.team.id + "/applications/" + $scope.config.application.id + "/monitor/edit")

        var data = JSON.stringify({
            id:$scope.monitor.id,
            enabled:$scope.monitor.enabled,
            frequency:$scope.monitor.frequency,
            period:$scope.monitor.period,
            hour:$scope.monitor.hour,
            minute:$scope.monitor.minute,
            day:$scope.monitor.day})

        $http.post(saveUrl,data,{headers:{'Content-Type':'application/json'}})
            .then(function successCallback(response) {
                $scope.successMessage = "Configuration saved."
            },function errorCallback(response) {
                $scope.errorMessage = "Configuration failed to save."
        });
    }


    $scope.switchTo = function(name) {
        $rootScope.$broadcast('modalSwitch', name, $scope.object);
    };
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };


    $(document).bind('click', function(event){
        $scope.successMessage = false
    })

});
