package com.rrd.generics;


import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TypeFactory {
	private static final Logger log = LoggerFactory.getLogger(TypeFactory.class);
	static class TraceSigVisitor extends SignatureVisitor{
		public TraceSigVisitor(int api) {
			super(api);
		}
		@Override
		public SignatureVisitor visitArrayType() {
			log.debug("visitArrayType()");
			return this;
		}
		@Override
		public void visitBaseType(char descriptor) {
			log.debug("visitBaseType({})",descriptor);
			super.visitBaseType(descriptor);
		}
		@Override
		public SignatureVisitor visitClassBound() {
			log.debug("visitClassBound()");
			return this;
		}
		@Override
		public void visitClassType(String name) {
			log.debug("visitClassType({})",name);
			super.visitClassType(name);
		}
		@Override
		public void visitEnd() {
			log.debug("visitEnd()");
			super.visitEnd();
		}
		@Override
		public SignatureVisitor visitExceptionType() {
			log.debug("visitExceptionType()");
			return this;
		}
		@Override
		public void visitFormalTypeParameter(String name) {
			log.debug("visitFormalTypeParameter({})",name);
			super.visitFormalTypeParameter(name);
		}
		@Override
		public void visitInnerClassType(String name) {
			log.debug("visitInnerClassType({})",name);
			super.visitInnerClassType(name);
		}
		@Override
		public SignatureVisitor visitInterface() {
			log.debug("visitInterface()");
			return this;
		}
		@Override
		public SignatureVisitor visitInterfaceBound() {
			log.debug("visitInterfaceBound()");
			return this;
		}
		@Override
		public SignatureVisitor visitParameterType() {
			log.debug("visitParameterType()");
			return this;
		}
		@Override
		public SignatureVisitor visitReturnType() {
			log.debug("visitReturnType()");
			return this;
		}
		@Override
		public SignatureVisitor visitSuperclass() {
			log.debug("visitSuperclass()");
			return this;
		}
		@Override
		public void visitTypeArgument() {
			log.debug("visitTypeArgument");
			super.visitTypeArgument();
		}
		@Override
		public SignatureVisitor visitTypeArgument(char wildcard) {
			log.debug("visitTypeArgument({})",wildcard);
			return this;
		}
		@Override
		public void visitTypeVariable(String name) {
			log.debug("visitTypeVariable({})",name);
			super.visitTypeVariable(name);
		}
		
	}
	public static Type buildType(String internalName,String signature){
		if(signature!=null){
			final Stack<Type> stack = new Stack<Type>();
			SignatureReader sr = new SignatureReader(signature);
			sr.accept(new TraceSigVisitor(Opcodes.ASM4));
			final AtomicReference<Type> ret = new AtomicReference<Type>();
			sr.accept(new SignatureVisitor(Opcodes.ASM4) {												
				boolean isArray = false;
				@Override
				public void visitClassType(String name) {					
					if(isArray){
						name = "[L"+name+";";
					}
					log.debug("push "+name);
					
					stack.push(forName(name));
					super.visitClassType(name);
				}
				@Override
				public SignatureVisitor visitArrayType() {
					isArray = true;
					return this;
				}
				@Override
				public void visitEnd() {
					isArray = false;
					Type childType = stack.pop();
					if(stack.isEmpty()){
						log.debug("{}",childType);
						ret.set(childType);
					}else{
						stack.peek().getGenericTypes().add(childType);
						log.debug("pop");
					}
				}
			});
			return ret.get();
		}else{
			return forName(internalName);
		}
	}
	
	static Type forName(String internalName){
		return new Type(classForName(internalName.replace("/",".")));										
	}
	
	static Class<?> classForName(String externalClassName){
		try {
			return Class.forName(externalClassName);				
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(String.format("Could not find %s",externalClassName),e);		
		}
	}
}
