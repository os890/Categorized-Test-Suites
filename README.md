# Categorized Test Suites

A JUnit 4 runner that auto-discovers and filters tests by custom category annotations,
enabling flexible test suite composition without explicit class lists.

## Overview

`CategorizedTestSuiteRunner` scans the entire test classpath and assembles suites
based on custom annotation categories, `@TestsOfType` type constraints, and
`@SkipTestCategory` exclusions. You define category annotations, annotate test classes,
and compose suites declaratively — no class listing required.

## Usage

### Define category annotations

```java
@TestCategory
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Nightly {}
```

### Annotate tests

```java
@Nightly
public class SlowIntegrationTest {
    @Test public void runSlowCheck() { ... }
}
```

### Compose suites

```java
// Runs all @Nightly tests
@Nightly
@RunWith(CategorizedTestSuiteRunner.class)
public class NightlySuite {}

// Runs all @Nightly tests that extend BaseIT, excluding @Flaky ones
@Nightly
@TestsOfType(BaseIT.class)
@SkipTestCategory(Flaky.class)
@RunWith(CategorizedTestSuiteRunner.class)
public class StableNightlySuite {}
```

## TestCategoryChecker

`TestCategoryChecker` is a CDI `@ApplicationScoped` bean that exposes annotation
inspection logic for use in CDI-managed environments:

```java
@Inject
TestCategoryChecker checker;

boolean isCategory = checker.isCategoryAnnotation(Nightly.class); // true
```

## Requirements

- Java 25+
- Maven 3.6.3+
- JUnit 4.13.2 (provided by the consuming project)
- Apache XBean Finder 4.27 (provided by the consuming project)

## Building

```bash
# Run the JUnit 4 categorized suite tests (default)
mvn clean verify

# Run the CDI SE tests
mvn clean verify -Pcdi-se
```

## Quality Plugins

The build enforces the following quality gates:

- **Compiler**: `-Xlint:all`, fail on warnings
- **Enforcer**: Java 25+, Maven 3.6.3+, dependency convergence, no javax.* dependencies
- **Checkstyle**: no star imports, brace enforcement, whitespace rules
- **Apache RAT**: Apache 2.0 license header verification
- **JaCoCo**: code coverage reporting
- **Surefire**: pinned version, profile-based test separation

## Testing

The project contains two test profiles:

- **`suite-tests`** (default): JUnit 4 suites demonstrating the `CategorizedTestSuiteRunner`
- **`cdi-se`**: CDI SE tests using the
  [Dynamic CDI Test Bean Addon](https://github.com/os890/dynamic-cdi-test-bean-addon)
  with `@EnableTestBeans` and Apache OpenWebBeans as the CDI implementation

## License

[Apache License, Version 2.0](LICENSE)
