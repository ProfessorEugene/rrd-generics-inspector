package com.rrd.generics;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Generic resolution utilities.
 * 
 * Can be used to resolve generic return types
 * 
 * @see #getConcreteReturnType()
 * @author erachitskiy
 *
 */
public class Resolver {
	private static final Logger log = LoggerFactory.getLogger(Resolver.class);
	private static final Map<StackTraceElement,Class<?>> returnTypeCache = 
			new ConcurrentHashMap<StackTraceElement, Class<?>>();
	/**
	 * Resolve the expected return type of a method with a generic return type by inspecting the caller.
	 * 
	 * For example:
	 * {@code
	 * 	Book book = deSerialize("...");
	 *  ...
	 *  <T> T deSerialize(String str){
	 *  	Class<?> myReturnType = Resolver.getConcreteReturnType();
	 *  	//The above will return "Book"
	 *  }
	 * }
	 * 		
	 * If no return type can be determined from the call stack, {@code java.lang.Object} is returned.
	 * @return concrete return type of current method
	 */
	public static Class<?> getConcreteReturnType(){
		/* find the caller */
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		log.debug("Attempting to resolve return type for {}#{}",stackTrace[1].getClassName(),stackTrace[1].getMethodName());
		final StackTraceElement caller = stackTrace[2];
		log.debug("Detected calling method of {}#{}",caller.getClassName(),caller.getMethodName());
		Class<?> concreteReturnType = returnTypeCache.get(caller);
		if(concreteReturnType != null){
			log.debug("Found cached instance of caller for a concrete return type of {}",concreteReturnType);
			return returnTypeCache.get(caller);
		}
		concreteReturnType = getConcreteReturnType(caller);
		if(concreteReturnType==null){
			concreteReturnType = Object.class;
		}		
		returnTypeCache.put(caller, concreteReturnType);
		log.debug("Resolved concrete return type of {}",concreteReturnType);
		return concreteReturnType;		
	}
	private static Class<?> getConcreteReturnType(final StackTraceElement caller){
		ClassReader cr;
		try {
			cr = new ClassReader(caller.getClassName());
		} catch (IOException e) {
			throw new RuntimeException(String.format("Could not read class %s",caller.getClassName()),e);
		}
		final AtomicReference<String> returnTypeInternalName = new AtomicReference<String>();
		cr.accept(new ClassVisitor(Opcodes.ASM4) {
			@Override
			public MethodVisitor visitMethod(int access, String name,
					String desc, String signature, String[] exceptions) {				
				if(caller.getMethodName().equals(name)){	
					log.debug("Found {}#{}",caller.getClassName(),caller.getMethodName());
					return new MethodVisitor(Opcodes.ASM4) {
						boolean onLine = false;
						public void visitLineNumber(int line, org.objectweb.asm.Label start) {
							if(line == caller.getLineNumber()){
								log.debug("Found line {}",line);
								onLine = true;								
							}else{
								onLine = false;
							}
						};
						public void visitTypeInsn(int opcode, String type) {
							if(onLine&&Opcodes.CHECKCAST==opcode){
								log.debug("Found a CHECKCAST operation to {}",type);
								returnTypeInternalName.set(type);
							}							
						};
					};					
				}else{				
					return super.visitMethod(access, name, desc, signature, exceptions);
				}
			}
		}, 0);
		String className = returnTypeInternalName.get();
		if(className!=null){
			String externalClassName = Type.getObjectType(className).getClassName();
			try {
				return Class.forName(externalClassName);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(String.format("Could not find %s",externalClassName),e);
			}
		}
		log.debug("Could not find appropriate CHECKCAST operation");
		return null;
	}
	
}
