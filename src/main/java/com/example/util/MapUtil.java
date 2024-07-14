package com.example.util;

import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;

public class MapUtil {
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> mapOf(Object ... args) {
		Map<K, V> map = new LinkedHashMap<>();
		for ( int i = 0 ; i < args.length ; i += 2) {
			map.put((K)args[i], (V)args[i+1]);
		}
		return map;
	}

	public static Map<String, String> parseQueryString(String qs, Charset charset) {
		if ( StringUtils.isEmpty(qs) )
			return Collections.emptyMap();
		Map<String, String> map = new LinkedHashMap<>();
		for ( String kv : qs.split("&") ) {
			int index = kv.indexOf("=");
			String key = null;
			String val = null;
			if ( index == -1 ) {
				key = URLDecoder.decode(kv, charset);
			} else {
				key = URLDecoder.decode(kv.substring(0, index), charset);
				val = URLDecoder.decode(kv.substring(index+1), charset);
			}
			map.put(key, val);
		}
		return map;
	}

	public static <T> T getAnyOf(Map<String, T> map, String ... keys) {
		for ( String key : keys ) {
			if ( map.containsKey(key) )
				return map.get(key);
		}
		return null;
	}

	public static Map<String, String> flatten(Object obj, String prefix) {
		Map<String, String> result = new HashMap<>();
		flatten(obj, prefix, result);
		return result;
	}

	public static void flatten(Object obj, String prefix, Map<String, String> result) {
		if (obj == null)
			return;

		if (obj instanceof List) {
			List list = (List) obj;
			for (int i = 0, n = list.size(); i < n; i++) {
				flatten(list.get(i), String.format("%s/%d", prefix, i), result);
			}
		} else if (obj instanceof Map) {
			Map<Object, Object> map = (Map) obj;
			for (Map.Entry<Object, Object> entry : map.entrySet()) {
				flatten(entry.getValue(), String.format("%s/%s", prefix, entry.getKey()), result);
			}
		} else {
			result.put(prefix, obj.toString());
		}
	}
}
