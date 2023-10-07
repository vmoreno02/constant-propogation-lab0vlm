package foldingInputs.ifStatement;

public class should_fold_givenFalseIfStatement {
    public void main () {
        int x = 0;

        if (false) {
            x = 4;
        }
        else {
            x = -1;
        }
    }
    
}
