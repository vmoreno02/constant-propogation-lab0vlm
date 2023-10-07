package foldingInputs.ifStatement;

public class should_fold_givenTrueIfStatement {
    public void main () {
        int x = 0;

        if (true) {
            x = 4;
        }
        else {
            x = -1;
        }
    }
}
