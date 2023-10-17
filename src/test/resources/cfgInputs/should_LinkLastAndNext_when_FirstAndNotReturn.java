package cfgInputs;

public class should_LinkLastAndNext_when_FirstAndNotReturn {
    int foo () {
        int x = 0;
        if (true) {
            x = 5;
        } else {
            x = 10;
        }
        x = x * 2;
    }

}
