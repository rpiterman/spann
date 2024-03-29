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

import java.io.Serializable;

import javax.persistence.EntityManager;

import com.masetta.spann.orm.jpa.beans.entitymanager.EntityManagerSupport;
import com.masetta.spann.orm.jpa.beans.entitymanager.EntityManagerSupportImpl;


public class BaseDaoImpl<T,PK extends Serializable> extends EntityManagerSupportImpl implements BaseDao<T,PK>{
	
	private EntityManagerSupport entityManagerSupport;
	
	private Class<T> entityType;

	public T find( PK id) {
		return getEntityManager().find( entityType, id );
	}

	public T merge(T entity) {
		return getEntityManager().merge( entity );
	}

	public void persist(T entity) {
		getEntityManager().persist( entity );
	}
	
	public void remove(T entity) {
		getEntityManager().remove( entity );
	}
	
	public void refresh(T entity) {
		getEntityManager().refresh( entity );
	}
	
	public void setEntityType(Class<T> entityType) {
		this.entityType = entityType;
	}

	public void setEntityManagerSupport(EntityManagerSupport entityManagerSupport) {
		this.entityManagerSupport = entityManagerSupport;
	}
	
}
