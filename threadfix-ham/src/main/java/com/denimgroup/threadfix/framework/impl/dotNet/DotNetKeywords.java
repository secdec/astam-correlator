////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s):
//              Denim Group, Ltd.
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.dotNet;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

/**
 * Created by mac on 6/11/14.
 */
final class DotNetKeywords {

    private DotNetKeywords() {}

    public static final String
            PUBLIC = "public",
            AREA = "Area",
            CLASS = "class",
            PARTIAL = "partial",
            INTERNAL = "internal",
            PROTECTED = "protected",
            PRIVATE = "private",
            STATIC = "static",
            ROUTE_CONFIG = "RouteConfig",
            STARTUP = "Startup",
            CONFIGURE ="Configure",
            REGISTER_ROUTES = "RegisterRoutes",
            ROUTE_COLLECTION = "RouteCollection",
            IAPPLICATION_BUILDER = "IApplicationBuilder", // .NET Core
            IROUTE_BUILDER = "IRouteBuilder", // .NET Core
            MAP_ROUTE = "MapRoute",
            NAMESPACE = "namespace",
            URL = "url",
            NAME = "name",
            DEFAULTS = "defaults",
            TEMPLATE = "template",
            NEW = "new",
            CONTROLLER = "controller",
            ACTION = "action",
            ID = "id",
            ROUTE = "Route",
            SYSTEM_HTTP_APPLICATION = "System.Web.HttpApplication";

    public static final List<String> RESULT_TYPES = list(
            "ActionResult",
            "IActionResult",
            "ContentResult",
            "JsonResult",
            "ViewResult",
            "HttpResponseMessage",
            "PartialViewResult",
            "RedirectResult",
            "RedirectToRouteResult",
            "JavaScriptResult",
            "FileResult",
            "EmptyResult",
            "IActionResult",
            "Task<IActionResult>"
    );

}
