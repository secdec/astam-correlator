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
