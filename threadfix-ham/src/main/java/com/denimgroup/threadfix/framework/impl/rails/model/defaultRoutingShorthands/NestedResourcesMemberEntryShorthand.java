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
