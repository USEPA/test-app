import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ MatrixTests.class, MethodsTests.class, OtherTests.class, QuickTests.class })
public class AllTests {

}
