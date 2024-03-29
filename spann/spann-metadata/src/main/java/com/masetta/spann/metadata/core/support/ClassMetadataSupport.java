/**
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.masetta.spann.metadata.core.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.masetta.spann.metadata.core.AnnotationMetadata;
import com.masetta.spann.metadata.core.ClassMetadata;
import com.masetta.spann.metadata.core.TypeArgument;
import com.masetta.spann.metadata.core.TypeMetadata;
import com.masetta.spann.metadata.core.TypeParameter;

/**
 * Provides convenience methods for dealing with {@link ClassMetadata} objects.
 * 
 * @author Ron Piterman
 */
public final class ClassMetadataSupport {

	private static final Map<String, Character> PRIMITIVE_TYPES = createPrimitiveTypesMap();

	private ClassMetadataSupport() {
	}

	private static Map<String, Character> createPrimitiveTypesMap() {
		Map<String, Character> map = new HashMap<String, Character>();
		addPrimitive(map, boolean.class, boolean[].class);
		addPrimitive(map, byte.class, byte[].class);
		addPrimitive(map, char.class, char[].class);
		addPrimitive(map, double.class, double[].class);
		addPrimitive(map, float.class, float[].class);
		addPrimitive(map, int.class, int[].class);
		addPrimitive(map, long.class, long[].class);
		addPrimitive(map, short.class, short[].class);
		return map;
	}

	private static void addPrimitive(Map<String, Character> map, Class<?> primtiveClass,
			Class<?> primitiveArrayClass) {
		map.put(primtiveClass.getCanonicalName(), primitiveArrayClass.getName().charAt(1));
	}

	/**
	 * Return the most specific generic type argument for type parameter
	 * <code>index</code> of the class/interface with the given name.
	 * <p>
	 * For example: for
	 * <p>
	 * <code>class Foo&lt;T> implemenets Validator&lt;T> {...}</code>
	 * <p>
	 * <code>class Bar extends Foo&ltString></code>
	 * <p>
	 * <code>findTypeParameterCapture( metadataOfBar , Validator.class.getCanonicalNam() , 0 );</code>
	 * <p>
	 * Will return the metadata of class String.
	 * 
	 * @param cmd
	 *            a class metadata which implements or extends the given generic
	 *            super type
	 * @param genericSuperType
	 *            a generic super class or super interface class name.
	 * @param index
	 *            the type parameter index of genericSuperType.
	 * 
	 * @return The most specific generic type capture of the given type
	 *         parameter index of <code>genericSuperType</code> in the hierarchy
	 *         of cmd.
	 */
	public static ClassMetadata findTypeParameterCapture(ClassMetadata cmd,
			String genericSuperType, int index) {
		TypeSearchArg type = new TypeSearchArg();
		findTypeArgument(type, cmd, genericSuperType, index, new HashSet<String>());
		return type.type;
	}

	private static void findTypeArgument(TypeSearchArg type, ClassMetadata cmd,
			String genericSuperType, int index, HashSet<String> visited) {

		if ( cmd == null )
			return;

		if ( !visited.add(cmd.getName()) ) {
			return;
		}

		// found interface
		if ( cmd.getName().equals(genericSuperType) ) {
			apply(type, cmd, index);
			return;
		}

		findTypeArgument(type, cmd.getSuperClass(true), genericSuperType, index, visited);

		for (ClassMetadata ifc : cmd.getInterfaces(true)) {
			findTypeArgument(type, ifc, genericSuperType, index, visited);
		}

		if ( type.name != null ) {
			int typeParamIndex = getTypeParameterIndex(cmd, type.name);
			if ( typeParamIndex > -1 ) {
				type.index = typeParamIndex;
				type.type = cmd.getTypeParameters().get(typeParamIndex).getType();
			}
			type.name = null;
		}

		if ( type.index != null ) {
			apply(type, cmd, type.index);
			return;
		}

	}

	private static void apply(TypeSearchArg type, ClassMetadata cmd, int index) {
		if ( cmd instanceof TypeMetadata ) {
			TypeArgument a = ((TypeMetadata) cmd).getTypeArguments().get(index);
			type.type = a.getType();
			type.name = a.getContextBoundTypeParameter();
		}
	}

	private static int getTypeParameterIndex(ClassMetadata cmd, String name) {
		int i = 0;
		for (TypeParameter tp : cmd.getTypeParameters()) {
			if ( tp.getName().equals(name) )
				return i;
			i++;
		}
		return -1;
	}

	private static class TypeSearchArg {
		private TypeSearchArg() {
		}

		private Integer index;
		private ClassMetadata type;
		private String name;
	}

	/**
	 * If the class underlying the given ClassMetadata is an instanceof the
	 * given typename.
	 * 
	 * @param cm the ClassMetadata which should be checked
	 * @param supertypeName a full qualified class name of a super type to check agains.
	 * @param dimension the array dimensions of supertypeName to check agains.
	 * 
	 * @return true if the given ClassMetadata is an instanceof the given supertypeName with
	 * 	the given array-dimensions.
	 */
	public static boolean instanceOf( ClassMetadata cm, String supertypeName, int dimension) {
		if ( cm == null )
			return false;

		if ( cm.getDimensions() != dimension ) {
			return false;
		}

		if ( cm.getName().equals(supertypeName) )
			return true;

		if ( instanceOf(cm.getSuperClass(false), supertypeName, 0) )
			return true;

		for (ClassMetadata ifc : cm.getInterfaces(false)) {
			if ( instanceOf(ifc, supertypeName, 0) )
				return true;
		}

		return false;
	}

	/**
	 * Returns a user friendly class name for the given ClassMetadata. The returned string
	 * will contain array-paretheses for each array dimension of the given ClassMetadata.
	 * 
	 * @param cm ClassMetadata to return a nice classname for.
	 * 
	 * @return a full qualified class name, suffixed with array parentheses (if needed).
	 */
	public static String getNiceClassnameFor(ClassMetadata cm) {
		return ClassMetadataSupport.getNiceClassnameFor(cm.getName(), cm.getDimensions());
	}
	
	/**
	 * Returns a user friendly class name for the given classname and array dimensions. T
	 * he returned string will contain array-paretheses for each array dimension of the given 
	 * classname.
	 * 
	 * @param classname the class name.
	 * @param dimensions the number of array dimensions of the class.
	 * 
	 * @return a full qualified class name, suffixed with array parentheses (if needed).
	 */
	public static String getNiceClassnameFor(String classname, int dimensions) {
		switch ( dimensions ) {
			case 0:
				return classname;
			case 1:
				return classname + "[]";
			case 2:
				return classname + "[][]";
			case 3:
				return classname + "[][][]";
			default:
				StringBuilder store = new StringBuilder(classname);
				for (int i = 0; i < dimensions; i++)
					store.append("[]");
				return store.toString();
		}
	}

	/**
	 * Return a class name that can be used by Class.forName(String) method for
	 * the given ClassMetadata.
	 * 
	 * @param cm ClassMetadata to retrieve the reflection class name for.
	 * 
	 * @return A standard java class name that may be used by Class.forName(String).
	 */
	public static String getReflectionClassnameFor(ClassMetadata cm) {
		return getReflectionClassnameFor(cm.getName(), cm.getDimensions());
	}

	/**
	 * Return a class name that can be used by Class.forName(String) method for
	 * the given classname and array dimensions.
	 * 
	 * @param classname the class name to use
	 * @param dimensions array dimensions of the created reflection-classname.
	 * 
	 * @return A standard java class name that may be used by Class.forName(String).
	 */
	public static String getReflectionClassnameFor(String classname, int dimensions) {
		boolean primitive = PRIMITIVE_TYPES.containsKey(classname);
		char arrayPrefix = primitive ? PRIMITIVE_TYPES.get(classname) : 'L';
		String classnameToUse = primitive ? "" : classname;
		switch ( dimensions ) {
			case 0:
				return classnameToUse;
			case 1:
				return "[" + arrayPrefix + classnameToUse;
			case 2:
				return "[[" + arrayPrefix + classnameToUse;
			case 3:
				return "[[[" + arrayPrefix + classnameToUse;
			default:
				StringBuilder store = new StringBuilder();
				for (int i = 0; i < dimensions; i++)
					store.append("[");
				store.append(arrayPrefix).append(classnameToUse);
				return store.toString();
		}
	}

	/**
	 * Resolve the given classMetadata to a java class.
	 * 
	 * @param classMetadata a ClassMetadata object to resolve to a java Class object.
	 * 
	 * @return the java Class object represented the given ClassMetadata object.
	 * @throws RuntimeException if the class is not found by the class loader attached 
	 * 		to the given class metadata.
	 */
	public static Class<?> resolve(ClassMetadata classMetadata) {
		try {
			ClassLoader cl = classMetadata.getClassLoader();
			String name = classMetadata.getName();
			if ( cl == null ) {
				return Class.forName( name );
			}
			else {
				return cl.loadClass( name );
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Create a new instance of the class represented by the given class metadata,
	 * casting it to the given type.
	 * 
	 * @param <T> the type to cast the instance to, typically an interface which is known
	 * 	to be a supertype of the given class metadtaa.
	 * @param type the type to cast the instacen to, typically an interface which is known
	 * 	to be a supertype of the given class metadtaa.
	 * @param classMetadata a ClassMetadata representing a subclass of the given type.
	 * 
	 * @return a new instance of the class represented by the given classMetadata, casted to the
	 * 		given type.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance( Class<T> type , ClassMetadata classMetadata ) {
		try {
			return (T) resolve( classMetadata ).newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException( e );
		} catch (IllegalAccessException e) {
			throw new RuntimeException( e );
		}
	}

}
