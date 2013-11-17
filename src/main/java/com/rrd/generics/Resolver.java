package com.rrd.generics;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic resolution utility class.  Can be used to resolve generic return types to concrete types at runtime.
 * For example:
 * {@code
 * 	Book book = deserialize("..");
 *  ..
 *  <T> T deserialize(String str){
 *  	Class<?> myReturnType = Resolver.getConcreteReturnType().getType();
 *  	//The above will return "Book"
 *  }
 * }
 * 
 * This utility depends on Labels with line numbers being compiled into any calling bytecode (this is default
 * behavior)
 * 
 * @see #getConcreteReturnType()
 * @see Type
 * @author erachitskiy
 *
 */
public class Resolver {
	private static final Logger log = LoggerFactory.getLogger(Resolver.class);
	
	/**
	 * Resolve the expected return type of a method with a generic return type by inspecting the call stack.
	 * 
	 * For example:
	 * {@code
	 * 	Book book = deSerialize("...");
	 *  ...
	 *  <T> T deSerialize(String str){
	 *  	Class<?> myReturnType = Resolver.getConcreteReturnType().getType();
	 *  	//The above will return "Book"
	 *  }
	 * }
	 * 		
	 * If no return type can be determined from the call stack, {@code java.lang.Object} is returned.
	 * 
	 * When available, the returned type contains generic information.
	 * 
	 * @return concrete return type of current method
	 */
	/* TODO: add caching */
	public static com.rrd.generics.Type getConcreteReturnType(){
		/* find the caller */
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		log.debug("Attempting to resolve return type for {}#{}",stackTrace[1].getClassName(),stackTrace[1].getMethodName());		
		com.rrd.generics.Type concreteReturnType = null;int i = 2;
		while(concreteReturnType == null && i<stackTrace.length){
			concreteReturnType = getConcreteReturnType(stackTrace[i++]);			
		}
		if(concreteReturnType==null){
			log.debug("Could not resolve concrete return type using CHECKCAST");
			concreteReturnType = new com.rrd.generics.Type(Object.class,null);
		}				
		log.debug("Resolved concrete return type of {}",concreteReturnType);
		return concreteReturnType;		
	}
	
	/**
	 * Use ASM to inspect a stack element, looking for a CHECKCAST operation within the scope of a line label
	 * pertaining to the stack element.  If one is found, the appropriate Type element is returned.  
	 * 
	 * On top of looking for CHECKCAST operations, this method attempts to resolve generic signature information
	 * by looking through local variables that might be assigned the concrete type.
	 * @param caller stack element
	 * @return {@code Type} or {@code null}
	 */
	private static com.rrd.generics.Type getConcreteReturnType(final StackTraceElement caller){
		log.debug("Starting inspection of {}#{}:{}",caller.getClassName(),caller.getMethodName(),caller.getLineNumber());
		ClassReader cr;
		try {
			cr = new ClassReader(caller.getClassName());
		} catch (IOException e) {
			throw new RuntimeException(String.format("Could not read class %s",caller.getClassName()),e);
		}
		final AtomicReference<String> returnTypeInternalName = new AtomicReference<String>();
		final AtomicReference<String> returnTypeInternalSig = new AtomicReference<String>();
		cr.accept(new ClassVisitor(Opcodes.ASM4) {
			@Override
			public MethodVisitor visitMethod(int access, String name,
					String desc, String signature, String[] exceptions) {				
				if(caller.getMethodName().equals(name)){	
					log.debug("Inspecting {}#{}",caller.getClassName(),caller.getMethodName());
					return new MethodVisitor(Opcodes.ASM4) {
						boolean onLine = false;
						Label firstLabelAfterLine = null;
						public void visitLineNumber(int line, Label start) {
							if(line == caller.getLineNumber()){
								log.debug("Found line {}",line);
								onLine = true;									
							}else{
								onLine = false;
							}
						};
						public void visitLabel(Label label) {
							/* attempt to remember the first label after the resolved line */
							/* in order to glean potential generic type information */
							if(onLine && firstLabelAfterLine==null){
								firstLabelAfterLine = label;
							}
						};
						public void visitInsn(int opcode) {
							if(onLine&&Opcodes.POP==opcode){
								/* TODO: test me */
								/* the caller doesn't care about return type because method invokation was discarded */
								log.debug("Call execution discarded; resolving return type as java/lang/Object");
								returnTypeInternalName.set("java/lang/Object");
							}
						};
						public void visitTypeInsn(int opcode, String type) {
							if(onLine&&Opcodes.CHECKCAST==opcode){
								log.debug("Found a CHECKCAST operation to {}",type);
								returnTypeInternalName.set(type);
							}							
						};
						public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
							/* compute generic type information from local variables if possible */							
							if(start == firstLabelAfterLine&&returnTypeInternalSig.get()==null){
								log.debug("Using {} as the generic signature for return type",signature);
								returnTypeInternalSig.set(signature);
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
			return TypeFactory.buildType(className, returnTypeInternalSig.get());			
		}
		log.debug("Could not find appropriate CHECKCAST operation in {}#{}",caller.getClassName(),caller.getMethodName());
		return null;
	}
	
}
