/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ps4os.categorizedtestsuite;

import jakarta.enterprise.context.ApplicationScoped;

import java.lang.annotation.Annotation;

/**
 * CDI bean that provides utility methods for inspecting test category annotations.
 *
 * <p>Inject this bean in CDI-managed environments to check whether an annotation
 * type has been designated as a {@link TestCategory}.</p>
 */
@ApplicationScoped
public class TestCategoryChecker
{
    /**
     * Checks whether the given annotation type is marked as a test category.
     *
     * @param annotationType the annotation type to inspect
     * @return {@code true} if {@code annotationType} is itself annotated with {@link TestCategory}
     */
    public boolean isCategoryAnnotation(Class<? extends Annotation> annotationType)
    {
        return annotationType.isAnnotationPresent(TestCategory.class);
    }
}
