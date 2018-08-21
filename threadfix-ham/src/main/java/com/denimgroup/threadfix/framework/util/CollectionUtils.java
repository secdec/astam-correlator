package com.denimgroup.threadfix.framework.util;

//  Effective copy of com.denimgroup.threadfix.CollectionUtils, but provides 'stringMap'
//  helper for CaseInsensitiveStringMap

import javax.annotation.Nonnull;

public class CollectionUtils {

	@Nonnull
	public static <V> CaseInsensitiveStringMap<V> stringMap() {
		return new CaseInsensitiveStringMap<V>();
	}

	@Nonnull
	public static <V> CaseInsensitiveStringMap<V> stringMap(String key1, V value1) {
		CaseInsensitiveStringMap<V> map = new CaseInsensitiveStringMap<V>();

		map.put(key1, value1);

		return map;
	}

	@Nonnull
	public static <V> CaseInsensitiveStringMap<V> stringMap(String key1, V value1,
	                                                        String key2, V value2) {
		CaseInsensitiveStringMap<V> map = new CaseInsensitiveStringMap<V>();

		map.put(key1, value1);
		map.put(key2, value2);

		return map;
	}

	@Nonnull
	public static <V> CaseInsensitiveStringMap<V> stringMap(String key1, V value1,
	                                                        String key2, V value2,
	                                                        String key3, V value3) {
		CaseInsensitiveStringMap<V> map = new CaseInsensitiveStringMap<V>();

		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);

		return map;
	}

	@Nonnull
	public static <V> CaseInsensitiveStringMap<V> stringMap(String key1, V value1,
	                                                        String key2, V value2,
	                                                        String key3, V value3,
	                                                        String key4, V value4) {
		CaseInsensitiveStringMap<V> map = new CaseInsensitiveStringMap<V>();

		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);

		return map;
	}


}
