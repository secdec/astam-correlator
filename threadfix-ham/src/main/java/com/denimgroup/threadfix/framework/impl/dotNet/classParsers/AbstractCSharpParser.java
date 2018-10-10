////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
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

package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import java.util.LinkedList;

public abstract class AbstractCSharpParser<T> {

    private T pendingItem = null;
    private LinkedList<T> finishedItems = new LinkedList<T>();
    private boolean disabled = false;

    protected final T getPendingItem() {
        return pendingItem;
    }

    protected final void setPendingItem(T item) {
        pendingItem = item;
    }

    protected final void finalizePendingItem() {
        if (pendingItem == null) {
            return;
        }

        finishedItems.add(pendingItem);
        pendingItem = null;
    }

    public final boolean isBuildingItem() {
        return pendingItem != null;
    }

    public final boolean hasItem() {
        return !finishedItems.isEmpty();
    }

    public final T pullCurrentItem() {
        return finishedItems.isEmpty() ? null : finishedItems.remove();
    }

    public final void clearItems() {
        finishedItems.clear();
    }

    public void reset() {
        clearItems();
        pendingItem = null;
    }

    public void resetAll() {
        reset();
    }

    protected final boolean isDisabled() {
        return disabled;
    }

    public void disable() {
        if (!disabled)
            disabled = true;
    }

    public void disableAll() {
        disable();
    }

    public void enable() {
        if (disabled)
            disabled = false;
    }

    public void enableAll() {
        enable();
    }

    public abstract void setParsingContext(CSharpParsingContext context);

}
