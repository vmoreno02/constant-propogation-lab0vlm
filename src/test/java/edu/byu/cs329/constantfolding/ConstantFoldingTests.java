package edu.byu.cs329.constantfolding;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import edu.byu.cs329.TestUtils;

@DisplayName("System tests for ConstantFolding")
public class ConstantFoldingTests {
    ConstantFolding folding = null;

    @BeforeEach
    void beforeEach() {
        folding = new ConstantFolding();
    }

    // @Test
    // @DisplayName("Given foldable, when fold, then all available folded")
    // public void given_foldable_when_fold_then_allAvailableFolded() {
    //     String rootName = "foldingInputs/constantFolding/should_fold_givenThingsToFold-root.java";
    //     String expectedName = "foldingInputs/constantFolding/should_fold_givenThingsToFold.java";
    //     TestUtils.assertFoldedAll(this, rootName, expectedName);
    // }

    // @Test
    // @DisplayName("Given not foldable, when fold, then no change")
    // public void given_notFoldable_when_fold_then_noChange() {
    //     String name = "foldingInputs/constantFolding/should_notFold.java";
    //     TestUtils.assertDidNotFoldAll(this, name);
    // }
}
