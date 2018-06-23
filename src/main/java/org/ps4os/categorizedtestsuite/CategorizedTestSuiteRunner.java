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

import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.ClassLoaders;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategorizedTestSuiteRunner extends Suite
{
    private static final Set<Class> ALL_CLASSES_WITH_TEST_ANNOTATIONS = new HashSet<>();
    private static final AnnotationFinder ANNOTATION_FINDER;

    static
    {
        ClassLoader classLoader = CategorizedTestSuiteRunner.class.getClassLoader();
        try
        {
            Set<URL> urls = ClassLoaders.findUrls(classLoader);
            ANNOTATION_FINDER = new AnnotationFinder(new ClasspathArchive(classLoader, urls));
            for (Method method : ANNOTATION_FINDER.findAnnotatedMethods(Test.class))
            {
                ALL_CLASSES_WITH_TEST_ANNOTATIONS.add(method.getDeclaringClass());
            }

            ALL_CLASSES_WITH_TEST_ANNOTATIONS.addAll(ANNOTATION_FINDER.findAnnotatedClasses(RunWith.class));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public CategorizedTestSuiteRunner(Class<?> clazz, RunnerBuilder builder) throws InitializationError
    {
        super(clazz, scanForTestClasses(clazz));
    }

    private CategorizedTestSuiteRunner(RunnerBuilder builder, Class<?>[] classes) throws InitializationError
    {
        super(builder, classes);
    }

    private CategorizedTestSuiteRunner(Class<?> clazz, Class<?>[] suiteClasses) throws InitializationError
    {
        super(clazz, suiteClasses);
    }

    private CategorizedTestSuiteRunner(RunnerBuilder builder, Class<?> clazz, Class<?>[] suiteClasses) throws InitializationError
    {
        super(builder, clazz, suiteClasses);
    }

    private CategorizedTestSuiteRunner(Class<?> clazz, List<Runner> runners) throws InitializationError
    {
        super(clazz, runners);
    }

    private static Class[] scanForTestClasses(Class<?> suiteClass)
    {
        Annotation customTestCategoryAnnotation = null;

        for (Annotation suiteClassAnnotation : suiteClass.getAnnotations())
        {
            if (suiteClassAnnotation.annotationType().isAnnotationPresent(TestCategory.class))
            {
                customTestCategoryAnnotation = suiteClassAnnotation;
                break;
            }
        }

        Set<Class> testClasses = new HashSet<>();

        if (customTestCategoryAnnotation != null)
        {
            Set<Class> candidateClasses = new HashSet<>();
            candidateClasses.addAll(ANNOTATION_FINDER.findAnnotatedClasses(customTestCategoryAnnotation.annotationType()));
            processTests(testClasses, suiteClass, candidateClasses);
        }
        else
        {
            processTests(testClasses, suiteClass, ALL_CLASSES_WITH_TEST_ANNOTATIONS);
        }
        return testClasses.toArray(new Class[testClasses.size()]);
    }

    private static void processTests(Set<Class> testClasses, Class<?> suiteClass, Set<Class> candidateClasses)
    {
        for (Class classToAdd : candidateClasses)
        {
            addClassIfNotRestricted(testClasses, suiteClass, classToAdd);
        }
    }

    private static void addClassIfNotRestricted(Set<Class> testClasses, Class<?> suiteClass, Class<?> classToAdd)
    {
        RunWith runWithAnnotation = classToAdd.getAnnotation(RunWith.class);

        if (runWithAnnotation == null || !Suite.class.isAssignableFrom(runWithAnnotation.value()))
        {
            TestsOfType testsOfTypeAnnotation = suiteClass.getAnnotation(TestsOfType.class);
            SkipTestCategory skipTestCategoryAnnotation = suiteClass.getAnnotation(SkipTestCategory.class);

            boolean addClass = true;
            if (testsOfTypeAnnotation != null && !testsOfTypeAnnotation.value().isAssignableFrom(classToAdd))
            {
                addClass = false;
            }
            else if (skipTestCategoryAnnotation != null && isRestrictedTestCategory(classToAdd, skipTestCategoryAnnotation))
            {
                addClass = false;
            }

            if (addClass)
            {
                testClasses.add(classToAdd);
            }
        }
    }

    private static boolean isRestrictedTestCategory(Class<?> classToAdd, SkipTestCategory skipTestCategoryAnnotation)
    {
        for (Class<? extends Annotation> annotationClass : skipTestCategoryAnnotation.value())
        {
            if (classToAdd.isAnnotationPresent(annotationClass))
            {
                return true;
            }
        }
        return false;
    }
}
