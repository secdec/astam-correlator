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

package com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingShorthands;


import com.denimgroup.threadfix.framework.impl.rails.model.RailsRoutingEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.RouteShorthand;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.CollectionEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.MemberEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.ResourcesEntry;
import com.denimgroup.threadfix.framework.impl.rails.model.defaultRoutingEntries.RootEntry;
import com.denimgroup.threadfix.framework.impl.rails.routeParsing.RailsConcreteRoutingTree;

import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

/**
 * A any entry directly within another 'resources' entry implicitly sets the inner entry base path to '{id}/...'
 *
 * ie:
 *
 * resources :users do
 *  resources :posts
 *  get :profile
 * end
 *
 * The "posts" base path is set to "users/{id}/posts/{id}" rather than "users/posts/{id}", effectively
 * placing the inner "posts" in a "member" entry:
 *
 * resources :users do
 *  member do
 *   resources :posts
 *   get :profile
 *  end
 * end
 */

public class NestedResourcesMemberEntryShorthand implements RouteShorthand {
	@Override
	public RailsRoutingEntry expand(RailsConcreteRoutingTree sourceTree, RailsRoutingEntry entry) {
		if (!(entry instanceof ResourcesEntry)) {
			return entry;
		}

		List<RailsRoutingEntry> subEntries = list();

		for (RailsRoutingEntry child : entry.getChildren()) {
			if (child instanceof RootEntry || child instanceof CollectionEntry || child instanceof MemberEntry) {
				continue;
			}

			subEntries.add(child);
		}

		if (!subEntries.isEmpty()) {
			MemberEntry memberEntry = new MemberEntry();
            memberEntry.setLineNumber(entry.getLineNumber());

			for (RailsRoutingEntry subEntry : subEntries) {
				memberEntry.addChildEntry(subEntry);
			}
			entry.addChildEntry(memberEntry);
		}

		return entry;
	}
}
