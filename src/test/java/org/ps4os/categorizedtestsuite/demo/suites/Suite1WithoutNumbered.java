package org.ps4os.categorizedtestsuite.demo.suites;

import org.junit.runner.RunWith;
import org.ps4os.categorizedtestsuite.CategorizedTestSuiteRunner;
import org.ps4os.categorizedtestsuite.SkipTestCategory;
import org.ps4os.categorizedtestsuite.demo.C1;
import org.ps4os.categorizedtestsuite.demo.Numbered;

//TestA
@C1
@SkipTestCategory(Numbered.class)
@RunWith(CategorizedTestSuiteRunner.class)
public class Suite1WithoutNumbered
{
}
