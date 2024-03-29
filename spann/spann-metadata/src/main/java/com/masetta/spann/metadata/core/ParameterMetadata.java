
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


/**
 * Metadata of a method parameter.
 * 
 * @author Ron Piterman
 */
public interface ParameterMetadata extends Metadata , AnnotatedElementMetadata {

    /**
     * Retrieve the class (type) of the parameter.
     *
     * @return a ClassMetadata object representing this parameter's type.
     */
    ClassMetadata getParameterClass();
    
    /**
     * Retrieve the generic type infomration for this parameter.
     *
     * @return the generic type of this parameter, or null if none.
     * 
     * @see FieldMetadata#getFieldType()
     */
    GenericType getParameterType();

}
