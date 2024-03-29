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

package com.masetta.spann.metadata.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Invocation handler for logging proxy.
 * @author Ron Piterman
 *
 */
class TraceInvocationHandler implements InvocationHandler {
	
	private final Object target;
	
	private final SpannLog log;
	
	/**
	 * <p>Constructor for TraceInvocationHandler.</p>
	 *
	 * @param target a {@link java.lang.Object} object.
	 * @param log a {@link com.masetta.spann.metadata.util.SpannLog} object.
	 */
	public TraceInvocationHandler(Object target,SpannLog log) {
		super();
		this.target = target;
		this.log = log == null ? SpannLogFactory.getLog( target.getClass() ) : log ;
	}

	/**
	 * <p>invoke</p>
	 *
	 * @param proxy a {@link java.lang.Object} object.
	 * @param method a {@link java.lang.reflect.Method} object.
	 * @param args an array of {@link java.lang.Object} objects.
	 * @return a {@link java.lang.Object} object.
	 * @throws java.lang.Throwable if any.
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		log.trace( method.getName() + "(" + Arrays.toString( args ) + ")");
		boolean ok = false;
		try {
			Object result = method.invoke( target, args );
			log.trace( method.getName() + " => " + result );
			ok = true;
			return result;
		}
		finally {
			if ( ! ok ) {
				log.trace( method.getName() + " ! exit with exception !" );
			}
		}
	}

}
