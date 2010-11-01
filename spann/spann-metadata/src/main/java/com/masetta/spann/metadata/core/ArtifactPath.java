
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

import java.util.Iterator;
public final class ArtifactPath implements Iterable<Metadata> {
    
    /** Constant <code>EMPTY_PATH</code> */
    public static final ArtifactPath EMPTY_PATH = new ArtifactPath();
    
    private final Metadata metadata;
    
    private final ArtifactPath parent;
    
    private ArtifactPath() {
        this.metadata = null;
        this.parent = null;
    }
    
    private ArtifactPath( ArtifactPath parent, Metadata metadata ) {
        this.metadata = metadata;
        this.parent = parent;
    }
    
    /**
     * <p>append</p>
     *
     * @param metadata a {@link com.masetta.spann.metadata.core.Metadata} object.
     * @return a {@link com.masetta.spann.metadata.core.ArtifactPath} object.
     */
    public ArtifactPath append( Metadata metadata ) {
        return new ArtifactPath( this , metadata );
    }
    
    /**
     * <p>size</p>
     *
     * @return a int.
     */
    public int size() {
        if ( parent == null )
            return 0;
        return 1 + parent.size();
    }
    
    /**
     * <p>iterator</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    public Iterator<Metadata> iterator() {
        return new PathIterator( this );
    }
    
    /**
     * <p>Getter for the field <code>metadata</code>.</p>
     *
     * @return a {@link com.masetta.spann.metadata.core.Metadata} object.
     */
    public Metadata getMetadata() {
        return this.metadata;
    }

    /**
     * <p>Getter for the field <code>parent</code>.</p>
     *
     * @return a {@link com.masetta.spann.metadata.core.ArtifactPath} object.
     */
    public ArtifactPath getParent() {
        return parent;
    }
    
    private static class PathIterator implements Iterator<Metadata> {
        
        private ArtifactPath path;
        
        public PathIterator(ArtifactPath path) {
            super();
            this.path = path;
        }

        public boolean hasNext() {
            return path.size() > 0;
        }

        public Metadata next() {
            ArtifactPath next = this.path;
            this.path = this.path.getParent();
            return next.metadata;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return "ArtifactPath [" + toStringInternal( "" ) + "]";
    }
    
    private String toStringInternal( String prefix ) {
        return metadata != null ? prefix + metadata + parent.toStringInternal(",") : ""; 
    }

}