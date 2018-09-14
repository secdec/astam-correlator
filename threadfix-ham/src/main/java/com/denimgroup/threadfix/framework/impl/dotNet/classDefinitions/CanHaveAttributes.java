package com.denimgroup.threadfix.framework.impl.dotNet.classDefinitions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

public class CanHaveAttributes {
    private Map<String, List<CSharpAttribute>> attributes = map();

    public void addAttribute(CSharpAttribute attribute) {
        String attributeName = attribute.getName();
        List<CSharpAttribute> storedAttributes = attributes.get(attributeName);

        if (storedAttributes == null) {
            storedAttributes = list();
            attributes.put(attribute.getName(), storedAttributes);
        }

        storedAttributes.add(attribute);
    }

    public CSharpAttribute getAttribute(String name) {
        List<CSharpAttribute> attributesWithName = attributes.get(name);
        if (attributesWithName == null) {
            return null;
        }

        return attributesWithName.get(0);
    }

    public List<CSharpAttribute> getAttributes() {
        List<CSharpAttribute> allAttributes = list();
        for (List<CSharpAttribute> attributeList : attributes.values()) {
            allAttributes.addAll(attributeList);
        }
        return allAttributes;
    }

    public List<CSharpAttribute> getAttributes(String... names) {
        List<CSharpAttribute> discoveredAttributes = list();
        for (String name : names) {
            List<CSharpAttribute> attributesWithName = attributes.get(name);
            if (attributesWithName == null) {
                continue;
            }

            discoveredAttributes.addAll(attributesWithName);
        }
        return discoveredAttributes;
    }
}
