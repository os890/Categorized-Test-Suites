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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CategorizedTestSuiteRunner extends Suite
{
    private static final Logger LOG = Logger.getLogger(CategorizedTestSuiteRunner.class.getName());

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
            AnnotationDB classScanner = createClassScanner(suiteClass, true, false);
            processTestsWithCategory(customTestCategoryAnnotation, testClasses, suiteClass, classScanner);
        }
        else
        {
            AnnotationDB classScanner = createClassScanner(suiteClass, false, true);
            processAllTests(testClasses, suiteClass, classScanner);
        }
        return testClasses.toArray(new Class[testClasses.size()]);
    }

    private static void processTestsWithCategory(Annotation customTestCategoryAnnotation, Set<Class> testClasses, Class<?> suiteClass, AnnotationDB classScanner)
    {
        Set<String> extractedResources = classScanner.getAnnotationIndex().get(customTestCategoryAnnotation.annotationType().getName());

        if (extractedResources == null || extractedResources.isEmpty())
        {
            return;
        }

        for (String className : extractedResources)
        {
            try
            {
                Class<?> classToAdd = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
                addClassIfNotRestricted(testClasses, suiteClass, classToAdd);
            }
            catch (Throwable t)
            {
                LOG.log(Level.SEVERE, "failed to process class " + className, t);
            }
        }
    }

    private static void processAllTests(Set<Class> testClasses, Class<?> suiteClass, AnnotationDB classScanner)
    {
        Set<String> extractedResources = classScanner.getAnnotationIndex().get(Test.class.getName());

        if (extractedResources == null || extractedResources.isEmpty())
        {
            return;
        }

        for (String className : extractedResources)
        {
            try
            {
                Class<?> classToAdd = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
                addClassIfNotRestricted(testClasses, suiteClass, classToAdd);
            }
            catch (Throwable t)
            {
                LOG.log(Level.SEVERE, "failed to process class " + className, t);
            }
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

    private static AnnotationDB createClassScanner(Class<?> suiteClass, boolean typeScanning, boolean methodScanning)
    {
        AnnotationDB annotationDB = new AnnotationDB();

        annotationDB.setScanFieldAnnotations(false);
        annotationDB.setScanParameterAnnotations(false);

        annotationDB.setScanClassAnnotations(typeScanning);
        annotationDB.setScanMethodAnnotations(methodScanning);

        try
        {
            annotationDB.scanArchives(ClasspathUrlFinder.findClassBase(suiteClass));
        }
        catch (IOException e)
        {
            LOG.log(Level.SEVERE, "failed to scan path", e);
        }

        for (URL classpathUrl : ClasspathUrlFinder.findClassPaths())
        {
            try
            {
                annotationDB.scanArchives(classpathUrl);
            }
            catch (IOException e)
            {
                LOG.log(Level.SEVERE, "failed to scan path", e);
            }
        }

        return annotationDB;
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
