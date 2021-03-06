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

package com.denimgroup.threadfix.framework.engine.framework;

import com.denimgroup.threadfix.data.enums.FrameworkType;
import com.denimgroup.threadfix.framework.engine.CachedDirectory;

import javax.annotation.Nonnull;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public abstract class FrameworkChecker {

    @Nonnull
    public abstract FrameworkType check(@Nonnull CachedDirectory directory);

    @Nonnull
    public List<FrameworkType> checkForMany(@Nonnull CachedDirectory directory) {
        FrameworkType framework = check(directory);
        if (framework != FrameworkType.NONE) {
            return list(framework);
        } else {
            return list();
        }
    }

    @Override
    public int hashCode() {
        return this.getClass().getName().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FrameworkChecker && this.hashCode() == other.hashCode();
    }

}
