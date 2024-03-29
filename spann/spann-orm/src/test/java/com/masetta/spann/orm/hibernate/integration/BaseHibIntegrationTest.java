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

package com.masetta.spann.orm.hibernate.integration;

import org.easymock.EasyMock;
import org.hibernate.Session;

import com.masetta.spann.orm.jpa.integration.BaseIntegrationTest;

public class BaseHibIntegrationTest extends BaseIntegrationTest {
	
	public BaseHibIntegrationTest() {
		setConfigLocation( "META-INF/daoApplicationContext-hibernate.xml");
	}
	
	protected Session expectSessionAccess() {
		Session s = createMock( Session.class );
		EasyMock.expect( entityManager.getDelegate() ).andReturn( s );
		return s;
	}

}
