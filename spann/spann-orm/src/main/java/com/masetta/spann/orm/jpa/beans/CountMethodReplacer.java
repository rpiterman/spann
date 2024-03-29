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

package com.masetta.spann.orm.jpa.beans;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.persistence.Query;

import com.masetta.spann.orm.jpa.support.QueryCount;
import com.masetta.spann.spring.base.method.beans.GenericMethodReplacer;
import com.masetta.spann.spring.util.Chain;
import com.masetta.spann.spring.util.ChainExecutor;
import com.masetta.spann.spring.util.Resolver;

public class CountMethodReplacer extends GenericMethodReplacer<QueryCallContext> {
	
	public static final String COUNT_CONTEXT_FACTORY_PROPERTY = "countContextFactory";
	
	public static final String COUNT_ARGUMENT_INDEX_PROPERTY = "countArgumentIndex";
	
	private Resolver<QueryCallContext,Object[]> countContextFactory;
	
	private Chain<Object,QueryCallContext>[] countChain;
	
	private Integer countArgumentIndex;
	
	@Override
	public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
		QueryCount c = (QueryCount) args[countArgumentIndex];
		if ( c != null )
			c.setCount( performCount( args ) );
		return super.reimplement(obj, method, args);
	}

	private Number performCount(Object[] args) {
		QueryCallContext ctx = countContextFactory.resolve( args );
		ChainExecutor<Object, QueryCallContext> executor =
			new ChainExecutor<Object, QueryCallContext>( countChain );
		Object result = executor.next( ctx );
		return (Number)result;
	}

	public void setCountContextFactory(Resolver<QueryCallContext, Object[]> countContextFactory) {
		this.countContextFactory = countContextFactory;
	}

	public void setCountArgumentIndex(Integer countArgumentIndex) {
		this.countArgumentIndex = countArgumentIndex;
	}
	
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		Chain<Object, QueryCallContext>[] contextHandlerChain = getContextHandlerChain();
		if ( contextHandlerChain == null ) {
			this.countChain = new Chain[] { CountResultStrategy.INSTANCE };
		}
		else {
			this.countChain = Arrays.copyOf( contextHandlerChain, contextHandlerChain.length + 1 );
			this.countChain[ this.countChain.length - 1 ] = CountResultStrategy.INSTANCE;
		}
	}

	private static class CountResultStrategy implements Chain<Object,QueryCallContext> {
		
		private static final CountResultStrategy INSTANCE = new CountResultStrategy();
		
		private CountResultStrategy() {}

		public Object perform(QueryCallContext param, ChainExecutor<Object, QueryCallContext> next) {
			final Query query = param.getQuery();
			query.setFirstResult( 0 );
			return (Number) query.getSingleResult();
		}
		
	}

}
