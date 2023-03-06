package pascal.taie.analysis.bugfinder;

import org.junit.Test;
import pascal.taie.analysis.Tests;
import pascal.taie.analysis.bugfinder.security.insecureapi.InsecureAPIUsageDetector;

public class InsecureAPITest {

    private static final String folderPath = "src/test/resources/bugfinder/InsecureAPI";

    void testInsecureAPI(String inputClass)
    {
        Tests.testInput(inputClass, folderPath, InsecureAPIUsageDetector.ID, "path: src/main/resources/insecureapi");
    }

    @Test
    public void test() {
        testInsecureAPI("RandomTest");
    }


}
