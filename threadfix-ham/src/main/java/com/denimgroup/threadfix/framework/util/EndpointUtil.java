package com.denimgroup.threadfix.framework.util;

import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.AbstractEndpoint;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class EndpointUtil {

    public static List<Endpoint> flattenWithVariants(@Nonnull List<Endpoint> endpoints) {
        List<Endpoint> result = list();
        Queue<Endpoint> pendingEndpoints = new ArrayDeque<Endpoint>(endpoints);

        while (!pendingEndpoints.isEmpty()) {
            Endpoint current = pendingEndpoints.remove();
            if (!result.contains(current)) {
                result.add(current);
                pendingEndpoints.addAll(current.getVariants());
            }
        }
        return result;
    }



    public interface VariantRectifier {
        void setPrimaryVariant(Endpoint assignee, Endpoint primaryVariant);
    }

    public static void rectifyVariantHierarchy(Collection<Endpoint> endpoints) {
        rectifyVariantHierarchy(endpoints, new VariantRectifier() {
            @Override
            public void setPrimaryVariant(Endpoint assignee, Endpoint primaryVariant) {
                ((AbstractEndpoint)assignee).setPrimaryVariant(primaryVariant);
            }
        });
    }

    public static void rectifyVariantHierarchy(Collection<Endpoint> endpoints, VariantRectifier rectifier) {
        List<Endpoint> visitedEndpoints = list();
        Queue<Endpoint> remainingEndpoints = new ArrayDeque<Endpoint>(endpoints);
        while (!remainingEndpoints.isEmpty()) {
            Endpoint next = remainingEndpoints.remove();
            if (visitedEndpoints.contains(next)) {
                continue;
            } else {
                visitedEndpoints.add(next);
            }
            for (Endpoint variant : next.getVariants()) {
                rectifier.setPrimaryVariant(variant, next);
                remainingEndpoints.add(variant);
            }
        }
    }
}
