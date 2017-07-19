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

package com.denimgroup.threadfix.webapp.controller;

import com.denimgroup.threadfix.data.entities.*;
import com.denimgroup.threadfix.remote.response.RestResponse;
import com.denimgroup.threadfix.service.ApplicationService;
import com.denimgroup.threadfix.service.ScheduledGitPollService;
import com.denimgroup.threadfix.service.util.PermissionUtils;
import com.denimgroup.threadfix.webapp.config.FormRestResponse;
import com.denimgroup.threadfix.webapp.validator.GitPollMonitorValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by jrios on 6/9/2017.
 */

@RestController
@RequestMapping("/organizations/{orgId}/applications/")
public class ApplicationGitMonitorController {

    @Autowired
    ApplicationService applicationService;

    @Autowired
    GitPollMonitorValidator gitPollMonitorValidator;

    @Autowired
    ScheduledGitPollService scheduledGitPollService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.setValidator(gitPollMonitorValidator);
    }



    //TODO resolve issue using GET on this method call. Remove edit path below
    @RequestMapping(value = "{appId}/monitor", method = RequestMethod.POST,consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public @ResponseBody Object getMonitor(@PathVariable("appId") int appId, @PathVariable("orgId") int orgId){

        if (!PermissionUtils.isAuthorized(Permission.CAN_MANAGE_APPLICATIONS, orgId, appId)) {
            RestResponse.failure("You don't have permission to manage application.");
        }
        ScheduledGitPoll monitor = null;
        Application application = applicationService.loadApplication(appId);
        if (application.getRepositoryType().equalsIgnoreCase(SourceCodeRepoType.GIT.getRepoType())) {
            monitor = scheduledGitPollService.loadByApplicationOrDefault(application);
        }
        return monitor;
    }



    @RequestMapping(value = "{appId}/monitor/edit", method = RequestMethod.POST,
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Object saveMonitor(@PathVariable("orgId") int orgId,
                     @PathVariable("appId") int appId,
                     @Valid @RequestBody ScheduledGitPoll monitor,
                    BindingResult result){

        if (!PermissionUtils.isAuthorized(Permission.CAN_MANAGE_APPLICATIONS, orgId, appId)) {
            RestResponse.failure("You don't have permission to manage application.");
        }
        if (result.hasErrors()) {
            return FormRestResponse.failure("Errors", result);
        }
        monitor.setApplication(applicationService.loadApplication(appId));
        if(scheduledGitPollService.save(monitor) < 0) {
            return RestResponse.failure("Failure to update the application monitor");
        }
        return RestResponse.success("Succeeded in updated the application monitor");
    }


}
