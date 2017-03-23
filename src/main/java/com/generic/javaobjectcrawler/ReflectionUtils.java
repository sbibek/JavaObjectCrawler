package com.generic.javaobjectcrawler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ReflectionUtils {
	/**
	 * Lists all fields related to the given class
	 * @param c class whose fields needs to be accessed
	 * @return
	 */
	public List<Field> getAllFields(Class c) {
		List<Field> fieldList = new ArrayList<>();
		fieldList.addAll(Arrays.asList(c.getDeclaredFields()));
		if (c.getSuperclass() != null)
			fieldList.addAll(getAllFields(c.getSuperclass()));
		return fieldList;
	}
	
	public List<Annotation> getMethodAnnotations(Class cls){
		List<Annotation> annotations = new ArrayList<>();
		for(Method method:cls.getMethods()){
			annotations.addAll(Arrays.asList(method.getAnnotations()));
		}
		return annotations;
	}
	
	public Map<Annotation, Method> getMethodAnnotationsMap(Class cls,Class annotationClass){
		Map<Annotation,Method> annotationsMap = new HashMap<>();
		for(Method method:cls.getMethods()){ 
			Annotation n = method.getAnnotation(annotationClass);
			if(n != null){
				annotationsMap.put(n, method);
			}
		}
		return annotationsMap;
	}
	
	/**
	 * read field from the field
	 * @param f field that needs to read
	 * @param o object from which the field to be read
	 * @return value of the filed in the given object
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public Object readField(Field f,Object o) throws IllegalArgumentException, IllegalAccessException{
		// set the field accessible to read the value
		f.setAccessible(true);
		return f.get(o);
	}
	
	/**
	 * Set the value of the field
	 * @param f the field that needs to be set
	 * @param o the object where the value needs to be set
	 * @param value value to be set
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void setFieldValue(Field f, Object o, Object value) throws IllegalArgumentException, IllegalAccessException{
		f.setAccessible(true);
		f.set(o, value);
	}
}
