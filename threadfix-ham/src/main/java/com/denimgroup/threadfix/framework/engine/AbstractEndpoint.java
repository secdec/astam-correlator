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

package com.denimgroup.threadfix.framework.engine;

import com.denimgroup.threadfix.data.entities.AuthenticationRequired;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.set;


public abstract class AbstractEndpoint implements Endpoint {

    @JsonIgnore
    List<Endpoint> variants = list();

    @JsonIgnore
    Endpoint primaryVariant = null;

    @Nonnull
    @Override
    public List<Endpoint> getVariants() {
        return variants;
    }

    @Override
    public boolean isVariantOf(Endpoint endpoint) {
        return endpoint == this || isImmediateVariant(this, endpoint);
    }

    public void setPrimaryVariant(Endpoint primaryVariant) {
        assert this != primaryVariant;
        this.primaryVariant = primaryVariant;
    }

    private static boolean isImmediateVariant(Endpoint a, Endpoint b) {
        if (a.isPrimaryVariant() && b.isPrimaryVariant() && a != b) {
            return false;

        } else if (a.isPrimaryVariant() && a.getVariants().contains(b)) {
            return true;

        } else if (b.isPrimaryVariant() && b.getVariants().contains(a)) {
            return true;

        } else if (a.getParentVariant() != null && a.getParentVariant().getVariants().contains(b)) {
            return true;

        } else if (b.getParentVariant() != null && b.getParentVariant().getVariants().contains(a)) {
            return true;

        } else {
            return false;

        }
    }

    public void addVariant(Endpoint variant) {
        assert this != variant;
        variants.add(variant);
    }

    public void addVariants(Collection<Endpoint> variants) {
        assert !variants.contains(this);
        this.variants.addAll(variants);
    }

    public void removeVariant(Endpoint variant) {
        this.variants.remove(variant);
    }

    public void clearVariants() {
        this.variants.clear();
    }

    @Override
    public Endpoint getParentVariant() {
        if (isPrimaryVariant()) {
            return this;
        }
        return primaryVariant;
    }

    @Override
    public boolean isPrimaryVariant() {
        return primaryVariant == null; // There is no primary variant for this endpoint, so this endpoint is the primary variant
    }

    @Override
    public int compareTo(@Nullable Endpoint otherEndpoint) {
        int returnValue = 0;

        if (otherEndpoint != null) {

            returnValue -= 2 * otherEndpoint.getFilePath().compareTo(getFilePath());

            if (getStartingLineNumber() < otherEndpoint.getStartingLineNumber()) {
                returnValue -= 1;
            } else {
                returnValue += 1;
            }
        }

        return returnValue;
    }

    @Override
    public int compareRelevance(String endpoint) {
        if (this.getUrlPath().equalsIgnoreCase(endpoint)) {
            return 1000;
        } else {
            return -1;
        }
    }

    // TODO finalize this
    @Nonnull
    @Override
    public String getCSVLine(PrintFormat... formats) {
        Set<PrintFormat> formatSet = set(formats);

        StringBuilder builder = new StringBuilder();

        if (formatSet.contains(PrintFormat.LINT)) {
            List<String> lintLines = getLintLine();

            if (!lintLines.isEmpty()) {
                String staticInformation = getStaticCSVFields();

                for (String lintLine : lintLines) {
                    builder.append(staticInformation).append(",").append(lintLine).append("\n");
                }

                builder.deleteCharAt(builder.length() - 1);
            } else {
                return getCSVLine(PrintFormat.DYNAMIC);
            }
        }

        if (formatSet.contains(PrintFormat.STATIC) && formatSet.contains(PrintFormat.DYNAMIC)) {
            builder.append(getStaticCSVFields()).append(',').append(getDynamicCSVFields());
        } else if (formatSet.contains(PrintFormat.STATIC)) {
            builder.append(getStaticCSVFields());
        } else if (!formatSet.contains(PrintFormat.LINT)) {
            builder.append(getDynamicCSVFields());
        }

        return builder.toString();
    }

    @Nonnull
    protected abstract List<String> getLintLine();

    protected String getDynamicCSVFields() {
        String parameters = getToStringNoCommas(getParameters());

        if (parameters.length() > 200) {
            parameters = parameters.substring(0, 200) + "...";
        }

        return getToStringNoCommas(getHttpMethod()) + "," +
                getUrlPath() + "," +
                parameters;
    }

    protected String getStaticCSVFields() {
        return getFilePath() + "," + getStartingLineNumber();
    }

    private String getToStringNoCommas(@Nonnull Object object) {
        return object.toString().replaceAll(",", "");
    }

    @Nonnull
    @Override
    public List<String> getRequiredPermissions() {
        return list();
    }

    @Nonnull
    @Override
    public String toString() {
        return getCSVLine();
    }

    @Nonnull
    @Override
    public AuthenticationRequired getAuthenticationRequired() {
        return AuthenticationRequired.UNKNOWN;
    }
}
