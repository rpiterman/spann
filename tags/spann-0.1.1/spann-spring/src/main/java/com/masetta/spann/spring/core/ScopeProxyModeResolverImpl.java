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

package com.masetta.spann.spring.core;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * Simple implementation of ScopeProxyModeResolver which resolves
 * all BeanDefinition's proxy mode to the same mode.
 * @author Ron Piterman    
 */
public class ScopeProxyModeResolverImpl implements ScopeProxyModeResolver {
    
    private ScopedProxyMode scopedProxyMode;
    
    public ScopeProxyModeResolverImpl(ScopedProxyMode scopedProxyMode) {
        super();
        this.scopedProxyMode = scopedProxyMode;
    }

    public ScopedProxyMode resolveScopedProxyMode(BeanDefinition beanDefinition) {
        return this.scopedProxyMode;
    }

}
