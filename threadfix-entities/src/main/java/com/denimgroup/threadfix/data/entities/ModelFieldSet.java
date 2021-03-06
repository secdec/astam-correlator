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
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.data.entities;

import com.denimgroup.threadfix.data.enums.ParameterDataType;

import javax.annotation.Nonnull;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.*;

public class ModelFieldSet implements Iterable<ModelField> {

    @Nonnull
    private Map<String, ModelField> fieldMap = map();

    @Nonnull
    private final Set<ModelField> fieldSet;

    public ModelFieldSet() {
        fieldSet = set();
    }

    public ModelFieldSet(@Nonnull Set<ModelField> fields) {
        fieldSet = fields;
        for (ModelField field : fields) {
            fieldMap.put(field.getParameterKey(), field);
        }
    }

    public ModelField getField(String parameterName) {
        return fieldMap.get(parameterName);
    }

    public boolean contains(ModelField field) {
        return fieldSet.contains(field);
    }

    public boolean contains(String paramName) {
        return getField(paramName) != null;
    }


    @Nonnull
    public ModelFieldSet add(ModelField field) {
        this.fieldSet.add(field);
        fieldMap.put(field.getParameterKey(), field);
        return this;
    }

    @Nonnull
    public ModelFieldSet addAll(@Nonnull ModelFieldSet beanFieldSet) {
        this.fieldSet.addAll(beanFieldSet.fieldSet);
        for (ModelField field : beanFieldSet.fieldSet) {
            fieldMap.put(field.getParameterKey(), field);
        }
        return this;
    }

    @Nonnull
    public Map<String, RouteParameter> getPossibleParameters() {
        Map<String, RouteParameter> parameters = map();
        for (ModelField field : fieldSet) {
            RouteParameter newParam = new RouteParameter(field.getParameterKey());
            newParam.setDataType(field.getType());
            newParam.setParamType(RouteParameterType.FORM_DATA);
            parameters.put(field.getParameterKey(), newParam);
        }
        return parameters;
    }

    @Override
    public String toString() {
        return fieldSet.toString();
    }

    public Set<ModelField> getFieldSet() {
        return fieldSet;
    }

    @Nonnull
    @Override
    public Iterator<ModelField> iterator() {
        return fieldSet.iterator();
    }

    public boolean isEmpty(){
        return fieldSet.isEmpty();
    }
}
