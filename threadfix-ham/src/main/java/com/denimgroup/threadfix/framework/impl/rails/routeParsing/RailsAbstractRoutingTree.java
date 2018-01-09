////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
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
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.rails.routeParsing;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RailsAbstractRoutingTree {

    RailsAbstractRouteEntryDescriptor rootDescriptor;

    public RailsAbstractRouteEntryDescriptor getRootDescriptor() {
        return rootDescriptor;
    }

    public void setRootDescriptor(RailsAbstractRouteEntryDescriptor rootDescriptor) {
        this.rootDescriptor = rootDescriptor;
    }


    public void walkTree(RailsAbstractTreeVisitor iterator) {
        walkTree(rootDescriptor, iterator);
    }

    public void walkTree(RailsAbstractRouteEntryDescriptor startNode, RailsAbstractTreeVisitor iterator) {
        iterator.acceptDescriptor(startNode);

        for (RailsAbstractParameter parameter : startNode.getParameters()) {
            iterator.acceptParameter(parameter);
        }

        for (RailsAbstractRouteEntryDescriptor node : startNode.getChildDescriptors()) {
            walkTree(node, iterator);
        }
    }


    public <Type extends RailsAbstractRouteEntryDescriptor> Collection<Type> findDescriptorsOfType(final Class type) {
        final List<Type> result = list();

        walkTree(new RailsAbstractTreeVisitor()
        {
            @Override
            public void acceptDescriptor(RailsAbstractRouteEntryDescriptor descriptor) {
                if (type.isAssignableFrom(descriptor.getClass())) {
                    result.add((Type) descriptor);
                }
            }

            @Override
            public void acceptParameter(RailsAbstractParameter parameter) {

            }

            @Override
            public void acceptInitializerParameter(RailsAbstractParameter parameter) {

            }
        });

        return result;
    }

    public <Type extends RailsAbstractParameter> Collection<Type> findParametersOfType(final Class type) {
        final List<Type> result = list();

        walkTree(new RailsAbstractTreeVisitor()
        {
            @Override
            public void acceptDescriptor(RailsAbstractRouteEntryDescriptor descriptor) {

            }

            @Override
            public void acceptParameter(RailsAbstractParameter parameter) {
                if (type.isAssignableFrom(parameter.getClass())) {
                    result.add((Type) parameter);
                }
            }

            @Override
            public void acceptInitializerParameter(RailsAbstractParameter parameter) {
                if (type.isAssignableFrom(parameter.getClass())) {
                    result.add((Type) parameter);
                }
            }
        });

        return result;
    }
}
