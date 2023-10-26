package propagationInputs;

public class should_foldEverything_given_foldable {
    int main() {
        int a = 30;
        int b = 9 + (a + 5);
        
        return b + (60 + a);
    }
}
