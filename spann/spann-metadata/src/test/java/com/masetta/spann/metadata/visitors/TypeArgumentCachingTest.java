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

package com.masetta.spann.metadata.visitors;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.masetta.spann.metadata.MetadataStore;
import com.masetta.spann.metadata.MetadataStoreImpl;
import com.masetta.spann.metadata.common.ClasspathResource;
import com.masetta.spann.metadata.core.ClassMetadata;
import com.masetta.spann.metadata.core.TypeMetadata;
import com.masetta.spann.metadata.reader.spring.SpringClassReaderAdapter;
import com.masetta.spann.metadata.rules.Rules;
import com.masetta.spann.metadata.visitors.classes.TypeArgumentTest1;
import com.masetta.spann.metadata.visitors.classes.TypeArgumentTest2;

public class TypeArgumentCachingTest {
	
    private MetadataStore store = new MetadataStoreImpl();
	
    @Test
	public void testTypeArgumentCaching() {
        VisitorControllerImpl vc = new VisitorControllerImpl( store , Rules.RULES_CLASS_ALL , Rules.LAZY_EAGER_ALL ,
        		new SpringClassReaderAdapter() );
        
        String ta1 = TypeArgumentTest1.class.getCanonicalName();
        String ta2 = TypeArgumentTest2.class.getCanonicalName();
        
        try {
			vc.visit( ClasspathResource.forClassname( ta1 , 
        			TypeArgumentTest1.class.getClassLoader() ) );
			vc.visit( ClasspathResource.forClassname( ta2 , 
        			TypeArgumentTest2.class.getClassLoader() ) );
        } catch ( RuntimeException ex ) {
        	ex.printStackTrace();
        	throw ex;
        }
        
        ClassMetadata md1 = store.getByClassname( ta1, 0 );
        ClassMetadata md2 = store.getByClassname( ta2, 0 );
        
        TypeMetadata md1s = (TypeMetadata) md1.getSuperClass( true );
        TypeMetadata md2s = (TypeMetadata) md2.getSuperClass( true );
        
        Assert.assertEquals( md1s.getTypeArguments().get( 0 ).getType().getName(), "java.lang.String" );
        Assert.assertEquals( md2s.getTypeArguments().get( 0 ).getType().getName(), "java.lang.Integer" );
	}

}
