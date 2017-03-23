package com.generic.javaobjectcrawler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Crawl interface will set targets and respective callbacks
 * for the match case. 
 * 
 * MATCH: what should the crawler match to call the callback method
 * 0. PARENT: Used for parent matching> Eg, if we want to crawl for String, name in User entity. entity here is PARENT 
 * 1. TYPE: If the given type (variable type) is matched while crawling, then call the callback [ * means any, will match everything ]
 * 2. NAME: This is the case where the name is to be matched to the variables while crawling. [ * means any, will match everything ] 
 * 
 * @author bibek
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Match {
	public String parent() default "*";
	
	public String type() default "*";
	
	public String name() default "*";
}
