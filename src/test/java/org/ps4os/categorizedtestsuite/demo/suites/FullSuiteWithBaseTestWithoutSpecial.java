package org.ps4os.categorizedtestsuite.demo.suites;

import org.junit.runner.RunWith;
import org.ps4os.categorizedtestsuite.CategorizedTestSuiteRunner;
import org.ps4os.categorizedtestsuite.SkipTestCategory;
import org.ps4os.categorizedtestsuite.TestsOfType;
import org.ps4os.categorizedtestsuite.demo.Special;
import org.ps4os.categorizedtestsuite.demo.tests.BaseTest;

//TestB1, TestB2
@TestsOfType(BaseTest.class)
@SkipTestCategory({ Special.class })
@RunWith(CategorizedTestSuiteRunner.class)
public class FullSuiteWithBaseTestWithoutSpecial
{
}
