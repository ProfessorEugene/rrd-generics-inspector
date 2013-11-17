package com.rrd.generics;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
@SuppressWarnings("unused")
public class ResolverTest {	
	Type rType = null;
	
	@BeforeClass
	public static void setUpLogger(){
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel","DEBUG");
	}
	
	@Before
	public void setUp(){
		rType = null;
	}
	
	
	@Test
	public void testStringType(){
		Long prefix=2l;String s = genericReturnType();Integer other = 12;
		assertEquals(String.class,rType.getType());
	}
	
	@Test
	public void testObjectType(){
		Object s = genericReturnType();
		assertEquals(Object.class,rType.getType());
	}
	
	@Test
	public void testMethodSet(){
		doWithString((String)genericReturnType());
		assertEquals(String.class,rType.getType());
	}
	
	@Test
	public void testPrimitiveIntegerType(){
		Integer i = genericReturnType();
		assertEquals(Integer.class,rType.getType());
	}
	
	@Test
	public void testCallChain(){
		class LocalClass{
			public <T> T localGenericReturnType(){
				return genericReturnType();
			}
		}
		Long l = new LocalClass().localGenericReturnType();
		assertEquals(Long.class,rType.getType());
	}
	
	@Test
	public void testCallChainWithComplexTypes(){
		class LocalClass{
			public <T> T localGenericReturnType(){
				return genericReturnType();
			}
		}
		List<String> l = new LocalClass().localGenericReturnType();
		assertEquals(List.class,rType.getType());
		assertEquals(String.class,rType.getGenericType(0).getType());
	}
	

	@Test
	public void testComplexTypes(){
		Map<String,List<Long>> complexType = genericReturnType();
		assertEquals(Map.class,rType.getType());
		assertEquals(String.class,rType.getGenericType(0).getType());
		assertEquals(List.class,rType.getGenericTypes().get(1).getType());
		assertEquals(Long.class,rType.getGenericType(1).getGenericType(0).getType());
	}
	
	@Test
	public void testNullType(){
		genericReturnType();
		assertEquals(Object.class,rType.getType());
	}
	
	public void doWithString(String str){
		
	}
	
	public <T> T genericReturnType(){
		rType = Resolver.getConcreteReturnType();		
		return null;
	}
}
