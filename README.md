rrd-generics-inspector
======================
A Java library that can be used to resolve concrete types for parametrized methods at runtime by inspecting the call stack. 

Overview
========
Consider the following method:
	
	public <T> T deserialize(String str){...}
	
The method's return type changes depending on the caller.  For example, when called using `Book book = deserialize("..")` , 
it is expected to return `Book`.  When called using `Author author = deserialize("..")`, it is expected to return `Author`.

Java APIs utilizing generic return types typically require hints via method parameters to resolve the expected return type.
For example:

	public <T> T deserialize(String str,Class<T> type){...}


This library provides an alternative:
	
	public <T> T deserialize(String str){
		Class<?> myReturnType = Resolver.getConcreteReturnType().getType();
		...
	}
	

By inspecting the call stack, the library figures out what the expected return type of any method.  When invoked via
`Book book = deserialize("..")`, `myReturnType` will be `Book`; likewise when invoked via `Author author = deserialize("..")`, 
`myReuturnType` will be `Author`.

Generic Types
=============
This library uses `com.rrd.generics.Type` to represent all Types.  This class attempts to model generics by containing
a concrete type and a list of generic/parameterized types.  In the examples above, `Book` and `Author` are not parameterized.

Now consier this invocation: 

	Map<String,List<Animal>> ark = deserialize("..")

In this case, `Resolver.getConcreteReturnType();` will return a `Type` instance that can be used to obtain generic type information:

		Type myReturnType = Resolver.getConcreteReturnType();
		myReturnType.toString(); // returns Map<String,List<Animal>>
		myReturnType.getType(); // returns Map.class
		myReturnType.getGenericType(0).getType(); // returns String.class
		myReturnType.getGenericType(1).getType(); // returns List.class
		myReturnType.getGenericType(1).getGenericType(1); // returns Animal.class


Limitations
===========
This library will only work when the code calling a method has been compiled with line number labels in place.  
Most Java code is indeed compiled this way so this is not a huge issue.

In case a return type can not be resolved - for example in the case of an invocation that doesn't process the returned reference:	

	for(String str:strs){
		deserialize(str); // note - nothing is done with returned reference
	}
	
`Resolver.getConcreteReturnType` will simply return a representation of `java.lang.Object`

How it works
============
This library works by inspecting the call stack of its caller.  This call stack typically includes class names, method names, and line numbers.
For each caller, the library inspects the calling bytecode using the ASM bytecode manipulation library.  By looking at the labels around the 
calling line number as well as local variables in the calling method, the library can detect 'CHECKCAST' operations and generic signatures.

Consider the following example:

	class Test{
		void test(){
			Set myLocalVariable = deserialize("..");		
		}
	}

A snippet of the bytecode for this example:

		  test() : void
		   L0
		    LINENUMBER 3 L0
		    ALOAD 0: this
		    LDC ".."
		    INVOKEVIRTUAL Test.deserialize(String) : Object
		    CHECKCAST java/util/Set
		    ASTORE 1
		   L1
		    LINENUMBER 4 L1
		    RETURN
		   L2
		    LOCALVARIABLE this Test L0 L2 0
		    LOCALVARIABLE myLocalVariable Set L1 L2 1
		    MAXSTACK = 2
		    MAXLOCALS = 2

When looking at a call stack from the `deserialize` method, one will encounter `Test#test:3`.  The above `Test` class bytecode can then
be inspected.

Note that line number '3' gets resolved to label "L0".  A "CHECKCAST" operation occurs within the scope of this label (between L0 and L1).  
Note that the cast operation tries to cast the returned object to "java/util/Set" - this is the expected return type of the `deserialize` method.

The callstack can be followed until either a `POP` (indicating method invokation results are discarded) or a `CHECKCAST` is detected.

Sometimes, generic data might also be available in the form of generic signatures for local variables that can be inspected.  In these cases, it 
is possible to inspect the signatures to produce more rich type information - like in the `Map<String,List<Animal>>` example above.



