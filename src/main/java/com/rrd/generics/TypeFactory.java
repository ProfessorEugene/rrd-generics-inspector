package com.rrd.generics;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;


public class TypeFactory {
	public static Type buildType(String internalName,String signature){
		if(internalName == null){
			throw new NullPointerException("Can not build type with null name");
		}
		String externalClassName = org.objectweb.asm.Type.getObjectType(internalName).getClassName();
		if(signature!=null){
			SignatureReader sr = new SignatureReader(signature);
			sr.accept(new SignatureVisitor(Opcodes.ASM4) {
				
			});
		}
		Type ret;
		try {
			ret = new Type(Class.forName(externalClassName), null);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(String.format("Could not find %s",externalClassName),e);		
		}
		return ret;
	}
}
