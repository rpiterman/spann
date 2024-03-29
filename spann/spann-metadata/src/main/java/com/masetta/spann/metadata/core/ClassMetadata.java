
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
 *
 * @author rpt
 * @version $Id: $
 */

package com.masetta.spann.metadata.core;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.masetta.spann.metadata.core.modifier.ClassModifier;
import com.masetta.spann.metadata.core.modifier.Modifier;

/**
 * Represents the metadata of a java class (or interface).
 * 
 * @author Ron Piterman
 */
public interface ClassMetadata extends Metadata , AnnotatedElementMetadata {
    
    /**
     * Retrieve the class loader to be used to load this class.
     * @return a ClassLoader to be used to load this class.
     */
    ClassLoader getClassLoader();
    
    /**
     * Retrieve the modifier of this class.
     *
     * @return a ClassModifier representing all modifier of this class.
     */
    ClassModifier getModifier();

    /**
     * Get the ClassMetadata of the superclass of the underlying class.
     *
     * @param loadSignatureIfNeeded load signature if not loaded yet. If signatur
     *     is already loaded (for example via {@link #getTypeParameters()}), loadSignatureIdNeeded
     *  has no effect. If signature is not loaded and this is 'true', signature will be loaded.
     *  and the returned ClassMetadta may be an instance of TypeMetadata.
     * @return A ClassMetatdata representing the super class of the class this metadata represents.
     *   if the superclass clause contains generic information (e.g. <code>extends Foo&lt;String></code>)
     *   and generic signature is loaded, this method will return a TypeMetadata.
     */
    ClassMetadata getSuperClass( boolean loadSignatureIfNeeded );
    
    /**
     * Get the interfaces the underlying class implements.
     *
     * @param loadSignatureIfNeeded load signature if not loaded yet. If signatur
     *     is already loaded (for example via {@link #getTypeParameters()}), loadSignatureIdNeeded
     *  has no effect. If signature is not loaded and this is 'true', signature will be loaded,
     *  and the returned set will contain any generic information available.
     * @return a {@link java.util.Set} object.
     */
    Set<ClassMetadata> getInterfaces( boolean loadSignatureIfNeeded );

    /**
     * Retrieve all the FieldMetadata representing the fields of the underlying class.
     *
     * @return an unmodifiable Set of all fields of the class.
     */
    Set<FieldMetadata> getFields();

    /**
     * Retrieve all the MethodMetadatas representing the methods of the underlying class.
     *
     * @return a Collection of all the methods of the class.
     */
    Collection<MethodMetadata> getMethods();

    /**
     * Generic type parameters as declared in the class name suffix
     * <code>public class Foo&lt;X...> {...</code>
     *
     * <p>
     * Each type parameter has a name and, if specified, a type (such as in
     * Foo&lt;X extends Serializable> )
     *
     * @return a {@link java.util.List} of all TypeParameters of the underlying class.
     */
    List<TypeParameter> getTypeParameters();
    
    /**
     * If this class metadata represents an array type,
     * return the number of array dimensions. Non-array classes return 0.
     * <p>
     * Note: contrary to the java reflection API, array metadata does not represent
     * the array class but the component class. In metadata objects representing array types,
     * all methods relate to the component-type of the array.
     * For example, the {@link #getName() name } of String[]'s metadata will be java.lang.String
     * and the {@link #getMethods() methods} will represent String's methods and not Array's.
     *
     * @return the number of array dimensions of the underlying class.
     */
    int getDimensions();
    
    /**
     * For inner classes only, the metadata of the outer class
     *
     * @return The ClassMetadata of the outer class, or null if this class is not in inner class.
     */
    ClassMetadata getOuterClass();
    
    /**
     * For inner classes only, the modifier of the inner class as defined
     * in the outer class.
     *
     * @return the Modifier of the inner class, or null if this class is not an inner class.
     */
    Modifier getOuterModifier();
    
    /**
     * For annotation interfaces only, retrieve the default value
     * of the given attribute, if any.
     * <p>
     * For more on the returned value, see {@link AnnotationMetadata#getAttribute(String, boolean) }.
     *
     * @param attribute attribute name to retrieve
     * @return the default value of the given annotation attribute.
     */
    Object getAnnotationAttributeDefault( String attribute );

	/**
	 * Return the simple name of this class, same as java Class's getSimeplName().
	 *
	 * @return the simple name of the class, without the package name.
	 */
	String getSimpleName();
    

}
