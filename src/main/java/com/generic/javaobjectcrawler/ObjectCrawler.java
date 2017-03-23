package com.generic.javaobjectcrawler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ObjectCrawler {
	ReflectionUtils util = new ReflectionUtils();
	InvocationHandler handler;

	public void crawl(Object subject, Object context) {
		// lets build invocation handler for this session
		handler = new InvocationHandler(context);

		// Lets get the goodies from the context
		Map<Annotation, Method> ann = util.getMethodAnnotationsMap(context.getClass(), Match.class);
		handler.buildInvocationTree(ann);

		// create the session variable before crawling
		List<String> hierarchy = new ArrayList<>();
		hierarchy.add(subject.getClass().getCanonicalName());
		Integer depth = 0;
		// initiate crawling
		this.initCrawling((Context) context, subject, hierarchy, depth);
	}

	private void initCrawling(Context crawlContext, Object subject, List<String> hierarchy, Integer depth) {
		// check max depth  
		//if(depth > crawlContext.getMaxDepth()) return;
		
		List<Field> fields = util.getAllFields(subject.getClass());
		//System.out.println("Crawling"+subject.getClass().getCanonicalName()+" # "+fields.size());
		try {
			for (Field f : fields) {
				// System.out.println(f.getType().getCanonicalName() + "
				// "+f.getName()+" "+crawlContext.isGenericType(f));

				// for the first time, lets just issue invoke if it matches any
				// of the rule
				f.setAccessible(true);
				handler.invoke(subject.getClass().getCanonicalName(), f.getType().getCanonicalName(), f.getName(),
						hierarchy, subject, f);
				// now lets see if this field is crawlable or not
				// also if the value of the field is null, we dont crawl it at
				// all
				// now we need to be very careful here
				// various conditons arise here
				// 1. What if the field is an array
				// 2. What if the field is List/Set (Generic type)
				// We can just continue to crawl if the field is crawlable and
				// is other than above mentioned things
				// so we allow normal crawling if( the field is not null, is not
				// array, is not generic type and is allowed to crawl)
				if (f.get(subject) != null && !crawlContext.isAllowableArrayType(f)
						&& !crawlContext.isAllowableGenericType(f) && crawlContext.isCrawlable(f.getType(), f)) {

					Object newSubject = f.get(subject);
					List<String> addedHierarchy = new ArrayList<>();
					addedHierarchy.addAll(hierarchy);
					addedHierarchy.add(newSubject.getClass().getCanonicalName());

					initCrawling(crawlContext, newSubject, addedHierarchy, depth + 1);
					
				} else {
					Object fieldObject = f.get(subject);
					// here we will have our different approach for different
					// types encountered
					if (fieldObject != null && crawlContext.isAllowableArrayType(f)) {

						List<String> newHierarchy = new ArrayList<>();
						newHierarchy.addAll(hierarchy);
						newHierarchy.add(fieldObject.getClass().getCanonicalName());

						Object[] arr = (Object[]) fieldObject;
						for (int i = 0; i < arr.length; i++) {
							initCrawling(crawlContext, arr[i], newHierarchy, depth + 1);
						}
					} else if (fieldObject != null && crawlContext.isAllowableGenericType(f)) {
						if (f.getType().getCanonicalName().equals(List.class.getCanonicalName())) {
							@SuppressWarnings("unchecked")
							List<Object> listObjects = (List<Object>) fieldObject;

							List<String> newHierarchy = new ArrayList<>();
							newHierarchy.addAll(hierarchy);
							newHierarchy.add(List.class.getCanonicalName());

							// if this is list, then we iterate it as list
							for (Object listObject : listObjects) {
								// now we invoke crawl on the object
								initCrawling(crawlContext, listObject, newHierarchy, depth + 1);
							}
						} else if (f.getType().getCanonicalName().equals(Set.class.getCanonicalName())) {
							@SuppressWarnings("unchecked")
							Set<Object> listObjects = (Set<Object>) fieldObject;

							List<String> newHierarchy = new ArrayList<>();
							newHierarchy.addAll(hierarchy);
							newHierarchy.add(Set.class.getCanonicalName());

							// if this is list, then we iterate it as list
							for (Object listObject : listObjects) {
								// now we invoke crawl on the object
								initCrawling(crawlContext, listObject, newHierarchy, depth + 1);
							}

						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
