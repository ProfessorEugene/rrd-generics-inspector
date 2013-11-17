package com.rrd.generics;


import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility for constructing {@link Type} objects from internal string representations of generic types.
 * @author erachitskiy
 *
 */
public class TypeFactory {
	private static final Logger log = LoggerFactory.getLogger(TypeFactory.class);
	
	/**
	 * Build a type for the supplied internal name and generic signature
	 * @param internalName a type internal name (e.g. java/lang/Object)
	 * @param signature an optional generic signature string (can be {@code null})
	 * @return {@code Type} instance for supplied internal name and generic signature
	 */
	public static Type buildType(String internalName,String signature){
		log.debug("Building type for {} / {}",internalName,signature);
		if(signature!=null){
			final Stack<Type> stack = new Stack<Type>();
			SignatureReader sr = new SignatureReader(signature);			
			final AtomicReference<Type> ret = new AtomicReference<Type>();
			sr.accept(new SignatureVisitor(Opcodes.ASM4) {												
				boolean isArray = false;
				@Override
				public void visitClassType(String name) {					
					if(isArray){
						name = "[L"+name+";";
					}
					log.debug("pushing type "+name);					
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
						log.debug("found {}",childType);
						ret.set(childType);
					}else{
						stack.peek().getGenericTypes().add(childType);						
					}
					log.debug("popping type");
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
