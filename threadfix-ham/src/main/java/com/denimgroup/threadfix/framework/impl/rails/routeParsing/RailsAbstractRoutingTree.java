package com.denimgroup.threadfix.framework.impl.rails.routeParsing;

import java.util.Collection;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class RailsAbstractRoutingTree {

    RailsAbstractRoutingDescriptor rootDescriptor;

    public RailsAbstractRoutingDescriptor getRootDescriptor() {
        return rootDescriptor;
    }

    public void setRootDescriptor(RailsAbstractRoutingDescriptor rootDescriptor) {
        this.rootDescriptor = rootDescriptor;
    }


    public void walkTree(RailsAbstractTreeVisitor iterator) {
        walkTree(rootDescriptor, iterator);
    }

    public void walkTree(RailsAbstractRoutingDescriptor startNode, RailsAbstractTreeVisitor iterator) {
        iterator.acceptDescriptor(startNode);

        for (RailsAbstractParameter parameter : startNode.getParameters()) {
            iterator.acceptParameter(parameter);
        }

        for (RailsAbstractRoutingDescriptor node : startNode.getChildDescriptors()) {
            walkTree(node, iterator);
        }
    }


    public <Type extends RailsAbstractRoutingDescriptor> Collection<Type> findDescriptorsOfType(final Class type) {
        final List<Type> result = list();

        walkTree(new RailsAbstractTreeVisitor()
        {
            @Override
            public void acceptDescriptor(RailsAbstractRoutingDescriptor descriptor) {
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
            public void acceptDescriptor(RailsAbstractRoutingDescriptor descriptor) {

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
