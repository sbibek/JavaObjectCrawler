package com.generic.javaobjectcrawler;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Context {
	
	private Integer maxDepth = 5;
	
	public Integer getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(Integer maxDepth) {
		this.maxDepth = maxDepth;
	}

	public List<Class<?>> getGenericTypeParameters(Field f) {
		Type t = f.getGenericType();

		List<Class<?>> typesList = new ArrayList<>();
		if (t instanceof ParameterizedType) {
			ParameterizedType p = (ParameterizedType) t;
			for (Type tp : p.getActualTypeArguments()) {
				typesList.add((Class<?>) tp);
			}
		}
		return typesList;
	}
	
	public Boolean isAllowableGenericType(Field f) {
		// here we say to the crawler what generic types to allow
		List<String> allowedGenericTypes = new ArrayList<>();
		allowedGenericTypes.add(List.class.getCanonicalName());
		allowedGenericTypes.add(Set.class.getCanonicalName());
		// beware that current implementation supports only these two as generic types
		// and hence can crawl on them
		if (allowedGenericTypes.contains(f.getType().getCanonicalName())) {
			// means, the type of generics is what we are already allowing;
			// now we need to know the parameter type to generics
			List<Class<?>> parameters = getGenericTypeParameters(f);
			// for now we just allow the one parameter generic
			if (parameters.get(0).getCanonicalName().startsWith("com.yco.dot")) {
				return true;
			}
		}
		return false;
	}

	public Boolean isCrawlable(Class<?> cls, Field f) {
		// the class can be generics such as list
		List<Class<?>> genericClasses = getGenericTypeParameters(f);
		// allow crawling only to the types that are defined inside our project
		if (cls.getCanonicalName().startsWith("com.yco.dot")) {
			return true;
		}

		return false;
	}

	public Boolean isAllowableArrayType(Field f) {
		String cannonicalType = f.getType().getCanonicalName();
		if (cannonicalType.startsWith("com.yco.dot") && cannonicalType.endsWith("[]")) {
			// means this is allowable array type
			return true;
		}
		return false;
	}
}
