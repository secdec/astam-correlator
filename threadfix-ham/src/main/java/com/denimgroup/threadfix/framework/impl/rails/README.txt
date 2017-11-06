In this folder is an implementation of a 3-pass Rails routes.rb parser. The design intends to separate language
parsing from the routes.rb APIs. These route APIs include the built-in route-entry types and may include third-party
route-entry types. By separating syntax parsing from entry parsing it becomes easier to add support for new
language features, API features and third-party APIs.

A route-entry refers to a line in routes.rb pertaining to route generation. For example, 'resource', 'get', 'match',
'concern', 'concerns', etc. are all route-entry types.

---

The 3 passes consist of:
1. A state-machine abstract syntax parser generating a call tree with identifier names and parameters
- RailsAbstractRoutesParser
- RailsAbstractRoutingTree
- RailsAbstractRoutingDescriptor
- RailsAbstractParameter
- RailsAbstractTreeVisitor

2. Reading and parsing the identifier and parameters of each abstract node into a typed implementation of
        RailsRoutingEntry by walking the abstract routing tree
- RailsConcreteRoutingTree
- RailsConcreteRoutingTreeBuilder
- RailsConcreteTreeVisitor
- RailsRoutingEntry and implementations (ie NamespaceEntry, ConcernsEntry, ResourceEntry, ..)
- RailsRouter and implementations (ie DefaultRailsRouter) identify entries by their identifiers and create an appropriate
        RailsRoutingEntry for that identifier

3. Applying 'shorthand transformations' which modify the routing tree by attaching parameters to certain route entries
- RouteShorthand and implementations (ie ConcernsEntryShorthand, ConcernsParameterShorthand)
- RailsRoutingEntry.getSupportedShorthands, etc.
- RailsConcreteRouteTreeMapper (applies shorthand transformations internally)

(4). The resulting RailsConcreteRoutingTree is ingested by the RailsConcreteRouteTreeMapper to generate the
        RailsEndpoints used by ThreadFix HAM.
- RailsConcreteRouteTreeMapper
- RailsConcreteRoutingTree
- RailsEndpoint

---

Extending or fixing syntax parsing belongs in pass 1, the RailsAbstractRoutesParser.

Adding support for new route-entry types belongs in pass 2. Implement a RailsRoutingEntry for your new route-entry type.
When using RailsConcreteRoutingTreeBuilder, route-entry types are instantiated based on the available RailsRouters.
Add its keyword to the DefaultRailsRouter, or create a new RailsRouter that identifies your route-entry type and
pass an instance to the RailsConcreteRoutingTreeBuilder. The source RailsAbstractRoutingTree is walked and parameters
are passed to each new RailsRoutingEntry implementation to interpret the parameters as appropriate. The resulting
RailsRoutingEntry objects are added to the resulting RailsConcreteRoutingTree.

Adding new tree transformations belongs in pass 3. Implement a RouteShorthand for your transformation type. Modify
the appropriate RouteEntry types to return your RouteShorthand implementation in their getSupportedShorthands
method.