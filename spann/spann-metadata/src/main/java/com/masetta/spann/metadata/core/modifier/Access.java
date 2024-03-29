
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

package com.masetta.spann.metadata.core.modifier;

/**
 * Represents different access levels of java artefacts : private, protected, default and public.
 * 
 * @author Ron Piterman
 */
public enum Access {
    
    PRIVATE,
    
    PROTECTED,
    
    DEFAULT,
    
    PUBLIC;
    
    /**
     * <p>isPrivate</p>
     *
     * @return a boolean.
     */
    public boolean isPrivate() {
        return equals( PRIVATE );
    }
    
    /**
     * <p>isProtected</p>
     *
     * @return a boolean.
     */
    public boolean isProtected() {
        return equals( PROTECTED );
    }
    
    /**
     * <p>isDefault</p>
     *
     * @return a boolean.
     */
    public boolean isDefault() {
        return equals( DEFAULT );
    }
    
    /**
     * <p>isPublic</p>
     *
     * @return a boolean.
     */
    public boolean isPublic() {
        return equals( PUBLIC );
    }
    
}
