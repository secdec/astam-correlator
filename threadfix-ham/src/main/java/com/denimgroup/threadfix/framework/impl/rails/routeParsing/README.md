     Copyright (C) 2017 Applied Visions - http://securedecisions.com

     The contents of this file are subject to the Mozilla Public License
     Version 2.0 (the "License"); you may not use this file except in
     compliance with the License. You may obtain a copy of the License at
     http://www.mozilla.org/MPL/

     Software distributed under the License is distributed on an "AS IS"
     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
     License for the specific language governing rights and limitations
     under the License.

     This material is based on research sponsored by the Department of Homeland
     Security (DHS) Science and Technology Directorate, Cyber Security Division
     (DHS S&T/CSD) via contract number HHSP233201600058C.

     Contributor(s):
              Secure Decisions, a division of Applied Visions, Inc


# Summary

In this folder is an implementation of a 3-pass Rails routes.rb parser. The design intends to separate language
parsing from the Rails routing APIs. These route APIs include the built-in route-entry types and may include third-party
route-entry types. By separating syntax parsing from entry parsing it becomes easier to add support for new
language features, API features and third-party APIs.

A route-entry refers to a line in routes.rb pertaining to route generation. For example, 'resource', 'get', 'match',
'concern', 'concerns', etc. are all route-entry types.

---

# Parser

The 3 passes consist of:
1. A basic lexer that gathers and organizes tokens into statements.
- RailsAbstractRoutesLexer
- RailsAbstractRoutingTree
- RailsAbstractRouteEntryDescriptor
- RailsAbstractParameter
- RailsAbstractTreeVisitor

2. Symbol resolution from method/route entry names to objects capable of parsing parameters for their associated symbol.
- RailsConcreteRoutingTree
- RailsConcreteRoutingTreeBuilder
- RailsConcreteTreeVisitor
- RailsRoutingEntry and implementations (ie NamespaceEntry, ConcernsEntry, ResourceEntry, ..)
- RailsRouter and implementations (ie DefaultRailsRouter) identify entries by their identifiers and create an appropriate
        RailsRoutingEntry for that identifier

3. Applying 'shorthand transformations' which modify the routing tree by attaching parameters, and replacing or inserting
        new route entries.
- RouteShorthand interface and implementations (ie ConcernsEntryShorthand, ConcernsParameterShorthand)
- RailsRoutingEntry.getSupportedShorthands
- RailsConcreteRouteTreeMapper (applies shorthand transformations internally)

(4). The resulting RailsConcreteRoutingTree is ingested by the RailsConcreteRouteTreeMapper to generate the
        RailsEndpoints used by ThreadFix HAM.
- RailsConcreteRouteTreeMapper
- RailsConcreteRoutingTree
- RailsEndpoint

---

# Extending and Maintenance

Extending or fixing syntax parsing belongs in pass 1, the RailsAbstractRoutesLexer.

Adding support for new route-entry types belongs in pass 2. Implement a RailsRoutingEntry for your new route-entry type.
When using RailsConcreteRoutingTreeBuilder, route-entry types are instantiated based on the available RailsRouters.
Add its keyword to the DefaultRailsRouter, or create a new RailsRouter that identifies your route-entry type and
pass an instance to the RailsConcreteRoutingTreeBuilder. The source RailsAbstractRoutingTree is walked and parameters
are passed to each new RailsRoutingEntry implementation to interpret the parameters as appropriate. The resulting
RailsRoutingEntry objects are added to the resulting RailsConcreteRoutingTree.

Adding new tree transformations belongs in pass 3. Implement a RouteShorthand for your transformation type. Modify
the appropriate RouteEntry types to return your RouteShorthand implementation in their getSupportedShorthands
method.

---

# API Notes

A RailsRoutingEntry has the following responsibilities:
- Provide the assigned module name, or the parent module name if available (recursively search parents)
- Provide the assigned controller name, or the parent controller name if available (recursively search parents)
- Provide PathHttpMethod objects that have fully-formed endpoints relative to the root, or null if an endpoint
            isn't relevant for the given route entry type (ie 'concern')



Inheriting from the AbstractRailsRoutingEntry provides various helper methods such as getParentModule (recursive),
makeRelativePathToParent (recursive), and others.

Nontrivial mapping of route entries to their endpoints can be ameliorated by implementing the route entry as
a data object and implementing a RoutingShorthand to manage the endpoint generation logic. If necessary, a shorthand
can modify a given object or replace it entirely. A shorthand may modify a RailsRoutingEntry but may not directly
add or remove entries to it. Entries may only be added and removed by returning a RailsRoutingEntry different from
the one that was passed to the shorthand, which will replace the original route entry that was passed in.

---

# Limitations

This implementation has the following known limitations:
- Ignores conditional statements
- Doesn't capture initializer parameters
- Doesn't handle many of the less-common parameters and shorthand permutations available in the Rails API
- Requires that RailsRoutingEntry implementations provide complete paths and resolved controller/module names
        (traversing the tree shouldn't be necessary within the node itself but it simplifies the current
         implementation)