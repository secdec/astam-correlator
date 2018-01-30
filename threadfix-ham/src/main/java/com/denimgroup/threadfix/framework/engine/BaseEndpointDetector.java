package com.denimgroup.threadfix.framework.engine;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class BaseEndpointDetector {

    private List<String> samples = list();

    public void addSample(String endpoint) {
        if (endpoint.startsWith("/")) {
            endpoint = endpoint.substring(1);
        }
        samples.add(endpoint);
    }

    public String detectBaseEndpoint() {
        List<String> mostCommonParts = list();
        for (String sample : samples) {
            String[] parts = StringUtils.split(sample, '/');
            if (mostCommonParts.isEmpty()) {
                mostCommonParts.addAll(Arrays.asList(parts));
            } else{
                while (parts.length < mostCommonParts.size()) {
                    mostCommonParts.remove(mostCommonParts.size() - 1);
                }

                int differentIndex = -1;
                for (int i = 0; i < parts.length && i < mostCommonParts.size(); i++) {
                    String samplePart = parts[i];
                    String commonPart = mostCommonParts.get(i);
                    if (!samplePart.equalsIgnoreCase(commonPart)) {
                        differentIndex = i;
                        break;
                    }
                }

                if (differentIndex >= 0) {
                    while (mostCommonParts.size() > differentIndex) {
                        mostCommonParts.remove(mostCommonParts.size() - 1);
                    }
                }
            }
        }
        return "/" + StringUtils.join(mostCommonParts, "/");
    }

}
