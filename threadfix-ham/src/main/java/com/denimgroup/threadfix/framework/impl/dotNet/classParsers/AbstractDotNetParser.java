package com.denimgroup.threadfix.framework.impl.dotNet.classParsers;

import java.util.LinkedList;

public abstract class AbstractDotNetParser<T> {

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

    public abstract void setParsingContext(DotNetParsingContext context);

}
