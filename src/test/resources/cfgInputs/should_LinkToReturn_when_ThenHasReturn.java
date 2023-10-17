package cfgInputs;

public class should_LinkToReturn_when_ThenHasReturn {
    int foo() {
        int x = 0;
        if (true) {
            return x;
        } else {
            return 5;
        }

        x = x * 2;
    }
}
