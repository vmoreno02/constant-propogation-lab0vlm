package edu.byu.cs329.constantfolding;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.byu.cs329.TestUtils;

@DisplayName("Tests for folding PrefixExpression types")
public class PrefixExpressionFoldingTests {
    PrefixExpressionFolding folder = null;

    @BeforeEach
    void beforeEach() {
        folder = new PrefixExpressionFolding();
    }

    @Test
    @DisplayName("Given root is null, when fold, then RuntimeException")
    void given_rootIsNull_when_fold_then_RuntimeException() {
        assertThrows(RuntimeException.class, () -> {
            folder.fold(null);
        });
    }

    @Test
    @DisplayName("Given root not CompilationUnit and orphan, when fold, then RuntimeException")
    void given_rootNotCompUnitAndOrphan_when_fold_then_RuntimeException() {
        assertThrows(RuntimeException.class, () -> {
            URI uri = TestUtils.getUri(this, "");
            ASTNode compilationUnit = TestUtils.getCompilationUnit(uri);
            ASTNode root = compilationUnit.getAST().newNullLiteral();
            folder.fold(root);
        });
    }

    @Test
    @DisplayName("Given no prefixed literals, when fold, then no change")
    void given_noPrefixedLiterals_when_fold_then_noChange() {
        String rootName = "foldingInputs/prefixExpressions/should_notFold_when_noPrefixedLiterals.java";
        String expectedName = "foldingInputs/prefixExpressions/should_notFold_when_noPrefixedLiterals.java";
        TestUtils.assertDidNotFold(this, rootName, expectedName, folder);
    }

    @Test
    @DisplayName("Given not true, when fold, then false")
    void given_notTrue_when_fold_then_false() {
        String rootName = "foldingInputs/prefixExpressions/should_fold_when_givenNotTruePrefixExpression-root.java";
        String expectedName = "foldingInputs/prefixExpressions/should_fold_when_givenNotTruePrefixExpression.java";
        TestUtils.assertDidFold(this, rootName, expectedName, folder);
    }

    @Test
    @DisplayName("Given not false, when fold, then true")
    void given_notFalse_when_fold_then_true() {
        String rootName = "foldingInputs/prefixExpressions/should_fold_when_givenNotFalsePrefixExpression-root.java";
        String expectedName = "foldingInputs/prefixExpressions/should_fold_when_givenNotFalsePrefixExpression.java";
        TestUtils.assertDidFold(this, rootName, expectedName, folder);
    }

}
