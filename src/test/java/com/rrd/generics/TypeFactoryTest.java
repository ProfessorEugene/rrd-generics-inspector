package com.rrd.generics;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class TypeFactoryTest {
	
	@BeforeClass
	public static void setUpLogger(){
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel","DEBUG");
	}
	
	@Test
	public void testSimpleType(){
		Type type = TypeFactory.buildType("java/lang/String",null);
		assertEquals(String.class,type.getType());
		assertEquals(0, type.getGenericTypes().size());
	}
	
	@Test
	public void testArrayType(){
		Type type = TypeFactory.buildType("[Ljava/lang/String;",null);
		assertEquals(String[].class,type.getType());
		assertEquals(0, type.getGenericTypes().size());
	}
	@Test
	public void testMultiArrayType(){		
		Type type = TypeFactory.buildType("[[Ljava/lang/String;",null);
		assertEquals(String[][].class,type.getType());
		assertEquals(0, type.getGenericTypes().size());
	}
	
	@Test
	public void testSimpleGenericType(){		
		//List<String>
		Type type = TypeFactory.buildType("java/util/List","Ljava/util/List<Ljava/lang/String;>;");
		assertEquals(List.class,type.getType());
		assertEquals(1,type.getGenericTypes().size());
		assertEquals(String.class,type.getGenericTypes().get(0).getType());
		assertEquals("java.util.List<java.lang.String>",type.toString());
	}
	@Test
	public void testComplexGenericType(){
		//Map<String,Map<String,List<? extends Number>>>
		Type type = TypeFactory.buildType("java/util/Map", "Ljava/util/Map<Ljava/lang/String;Ljava/util/Map<Ljava/lang/String;Ljava/util/List<+Ljava/lang/Number;>;>;>;");
		assertEquals(Map.class,type.getType());
		assertEquals(2,type.getGenericTypes().size());
		assertEquals(String.class,type.getGenericTypes().get(0).getType());
		assertEquals(2,type.getGenericTypes().get(1).getGenericTypes().size());
		assertEquals(Map.class,type.getGenericTypes().get(1).getType());
		assertEquals(String.class,type.getGenericTypes().get(1).getGenericTypes().get(0).getType());
		assertEquals(List.class,type.getGenericTypes().get(1).getGenericTypes().get(1).getType());
		assertEquals(1,type.getGenericTypes().get(1).getGenericTypes().get(1).getGenericTypes().size());
		assertEquals(Number.class,type.getGenericTypes().get(1).getGenericTypes().get(1).getGenericTypes().get(0).getType());
		assertEquals("java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.util.List<java.lang.Number>>>",type.toString());
	}
	
	@Test
	public void testGenericArrayType(){
		//List<String[]>
		Type type = TypeFactory.buildType("java/util/List", "Ljava/util/List<[Ljava/lang/String;>;");
		assertEquals(List.class,type.getType());
		assertEquals(1,type.getGenericTypes().size());
		assertEquals(String[].class,type.getGenericTypes().get(0).getType());
		assertEquals("java.util.List<[Ljava.lang.String;>",type.toString());
	}
}
