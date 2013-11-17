package com.rrd.generics;

import java.util.LinkedList;
import java.util.List;

/**
 * A container for representing potentially parameterized types
 * @author erachitskiy
 *
 */
public class Type {
	private final List<Type> genericTypes = new LinkedList<Type>();
	private final Class<?> type;
	
	Type(Class<?> type){
		this.type = type;
	}		
	
	Type(Class<?> type, List<Type> genericTypes) {		
		this.type = type;
		if(genericTypes!=null){
			this.genericTypes.addAll(genericTypes);
		}
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
	/**
	 * Get the generic type at supplied index
	 * @param index index of type
	 * @return generic type at supplied index
	 */
	public Type getGenericType(int index){
		return getGenericTypes().get(index);
	}
	
	@Override
	public String toString() {
		if(genericTypes.isEmpty()){
			return type.getName();
		}else{
			StringBuilder sb = new StringBuilder(type.getName());
			sb.append("<");
			boolean firstGenType = true;
			for(Type genericType:genericTypes){
				if(!firstGenType){
					sb.append(",");
				}
				sb.append(genericType.toString());
				firstGenType = false;
			}
			sb.append(">");
			return sb.toString();
		}	
	}
}
