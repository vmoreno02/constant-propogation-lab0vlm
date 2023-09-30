package foldingInputs.ifStatement;

public class should_notFold_givenIfElseBlock {
    public void main() {
        int x = 0;

        if (false) {
            x = 2;
        } else if (true) {
            x = 10;
        }
    }
}
