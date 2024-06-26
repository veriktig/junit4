package junit.framework;

import java.util.List;

import org.junit.Ignore;
import org.junit.runner.Describable;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.Orderer;
import org.junit.runner.manipulation.InvalidOrderingException;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Orderable;
import org.junit.runner.manipulation.Sorter;

/**
 * The JUnit4TestAdapter enables running JUnit-4-style tests using a JUnit-3-style test runner.
 *
 * <p> To use it, add the following to a test class:
 * <pre>
      public static Test suite() {
        return new JUnit4TestAdapter(<em>YourJUnit4TestClass</em>.class);
      }
</pre>
 */
public class JUnit4TestAdapter implements Test, Filterable, Orderable, Describable {
    private final Class<?> fNewTestClass;

    private final Runner fRunner;

    private final JUnit4TestAdapterCache fCache;

    public JUnit4TestAdapter(Class<?> newTestClass) {
        this(newTestClass, JUnit4TestAdapterCache.getDefault());
    }

    public JUnit4TestAdapter(final Class<?> newTestClass, JUnit4TestAdapterCache cache) {
        fCache = cache;
        fNewTestClass = newTestClass;
        fRunner = Request.classWithoutSuiteMethod(newTestClass).getRunner();
    }

    @Override
    public int countTestCases() {
        return fRunner.testCount();
    }

    @Override
    public void run(TestResult result) {
        fRunner.run(fCache.getNotifier(result, this));
    }

    // reflective interface for Eclipse
    public List<Test> getTests() {
        return fCache.asTestList(getDescription());
    }

    // reflective interface for Eclipse
    public Class<?> getTestClass() {
        return fNewTestClass;
    }

    @Override
    public Description getDescription() {
        Description description = fRunner.getDescription();
        return removeIgnored(description);
    }

    private Description removeIgnored(Description description) {
        if (isIgnored(description)) {
            return Description.EMPTY;
        }
        Description result = description.childlessCopy();
        for (Description each : description.getChildren()) {
            Description child = removeIgnored(each);
            if (!child.isEmpty()) {
                result.addChild(child);
            }
        }
        return result;
    }

    private boolean isIgnored(Description description) {
        return description.getAnnotation(Ignore.class) != null;
    }

    @Override
    public String toString() {
        return fNewTestClass.getName();
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        filter.apply(fRunner);
    }

    @Override
    public void sort(Sorter sorter) {
        sorter.apply(fRunner);
    }

    /**
     * {@inheritDoc}
     *
     * @since 4.13
     */
    @Override
    public void order(Orderer orderer) throws InvalidOrderingException {
        orderer.apply(fRunner);
    }
}
