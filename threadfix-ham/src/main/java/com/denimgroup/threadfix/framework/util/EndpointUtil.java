package com.denimgroup.threadfix.framework.util;

import com.denimgroup.threadfix.data.interfaces.Endpoint;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
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

}
