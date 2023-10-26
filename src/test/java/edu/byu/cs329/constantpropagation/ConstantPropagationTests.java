package edu.byu.cs329.constantpropagation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import edu.byu.cs329.TestUtils;


@DisplayName("Tests for ConstantPropagation")
public class ConstantPropagationTests {
    @Test
    @Tag("Propagation")
    @DisplayName("Given empty file, when propagate, then no change")
    public void given_emptyFile_when_propagate_then_noChange() {
        String rootName = "propagationInputs/empty.java";
        String expectedName = "propagationInputs/empty2.java";
        init(rootName);
        assertTrue(diffChecker(this, rootName, expectedName));
    }

    @Test
    @Tag("Propagation")
    @DisplayName("Given non foldable, when propagate, then no change")
    public void given_nonFoldable_when_propagate_then_noChange() {
        String rootName = "propagationInputs/should_notFold_given_unfoldable.java";
        String expectedName = "propagationInputs/should_notFold_given_unfoldable2.java";
        init(rootName);
        assertTrue(diffChecker(this, rootName, expectedName));
    }

    @Test
    @Tag("Propagation")
    @DisplayName("Given fully foldable, when propagate, folds everything")
    public void given_fullyFoldable_when_propagate_then_foldsEverything() {
        String rootName = "propagationInputs/should_foldEverything_given_foldable-root.java";
        String expectedName = "propagationInputs/should_foldEverything_given_foldable.java";
        init(rootName);
        assertTrue(diffChecker(this, rootName, expectedName));
    }

    @Test
    @Tag("Propagation")
    @DisplayName("Given partly foldable, when propagate, folds partway")
    public void given_fullyFoldable_when_propagate_then_foldsPartway() {}

    void init(String fileName) {
        ASTNode node = TestUtils.getASTNodeFor(this, fileName);
        ConstantPropagation.propagate(node);
    }

    static boolean diffChecker(Object t, String rootName, String expectedName) {
        ASTNode root = TestUtils.getASTNodeFor(t, rootName);
        ASTNode expected = TestUtils.getASTNodeFor(root, expectedName);
        return expected.subtreeMatch(new ASTMatcher(), root);
    }
}
