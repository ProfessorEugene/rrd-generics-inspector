package com.rrd.generics;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
@SuppressWarnings("unused")
public class ResolverTest {	
	Class<?> rType = null;
	
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
		assertEquals(String.class,rType);
	}
	
	@Test
	public void testObjectType(){
		Object s = genericReturnType();
		assertEquals(Object.class,rType);
	}
	
	@Test
	public void testMethodSet(){
		doWithString((String)genericReturnType());
		assertEquals(String.class,rType);
	}
	
	@Test
	public void testPrimitiveIntegerType(){
		int i = genericReturnType();
		assertEquals(Integer.class,rType);
	}
	
	@Test
	public void testCallChain(){
		class LocalClass{
			public <T> T localGenericReturnType(){
				return genericReturnType();
			}
		}
		Long l = new LocalClass().localGenericReturnType();
		assertEquals(Long.class,rType);
	}

	@Test
	public void testCollections(){
		Map<String,List<Long>> complexType = genericReturnType();
		//complexType.get("fa").add(1l);
		assertEquals(Map.class,rType);
	}
	
	@Test
	public void testNullType(){
		genericReturnType();
		assertEquals(Object.class,rType);
	}
	
	public void doWithString(String str){
		
	}
	
	@SuppressWarnings("unchecked")
	public <T> T genericReturnType(){
		rType = Resolver.getConcreteReturnType().getType();
		if(Integer.class == rType){
			return (T)new Integer(1);
		}
		return null;
	}
}
