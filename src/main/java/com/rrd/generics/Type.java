package com.rrd.generics;

import java.util.List;

/**
 * A container for representing potentially parameterized types
 * @author erachitskiy
 *
 */
public class Type {
	private final List<Type> genericTypes;
	private final Class<?> type;
	
	
	
	Type(Class<?> type, List<Type> genericTypes) {		
		this.type = type;
		this.genericTypes = genericTypes;
	}
	
	/**
	 * Get the concrete type class
	 * @return concrete type class
	 */
	public Class<?> getType(){
		return type;
	}
	/**
	 * Get the list of resolved parameterized type information for this type
	 * @return list of resolved parameterized type information for this type
	 */
	public List<Type> getGenericTypes(){
		return genericTypes;
	}
}
