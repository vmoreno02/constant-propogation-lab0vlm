package foldingInputs.ifStatement;

public class should_fold_givenOnlyFalse {
    public void main() {
        int x = 0;

        if (false) {
            x = -1;
        }
    }
}