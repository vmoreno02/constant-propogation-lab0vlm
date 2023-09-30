package edu.byu.cs329.constantfolding;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.byu.cs329.TestUtils;

@DisplayName("Tests for InfixExpressionExtendedFolding")
public class InfixExpressionExtendedFoldingTests {
    InfixExpressionExtendedFolding folder = null;

    @BeforeEach
    void beforeEach() {
        folder = new InfixExpressionExtendedFolding();
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
    @DisplayName("Given no sum expressions, when fold, then no change")
    void given_noSumExpressions_when_fold_then_noChange() {
        String rootName = "foldingInputs/infixExtendedExpressions/should_notFold_givenNoSumInfix.java";
        String expectedName = "foldingInputs/infixExtendedExpressions/should_notFold_givenNoSumInfix.java";
        TestUtils.assertDidNotFold(this, rootName, expectedName, folder);
    }

    @Test
    @DisplayName("Given sum expression, when fold, then one number literal")
    void given_sumExpression_when_fold_then_oneNumberLiteral() {
        String rootName = "foldingInputs/infixExtendedExpressions/should_fold_givenExtendedInfixExpression-root.java";
        String expectedName = "foldingInputs/infixExtendedExpressions/should_fold_givenExtendedInfixExpression.java";
        TestUtils.assertDidFold(this, rootName, expectedName, folder);
    }

    @Test
    @DisplayName("Given sum expression with minus, when fold, then two number literals")
    void given_sumExpressionWithMinus_when_fold_then_twoNumberLiterals() {
        String rootName = "foldingInputs/infixExtendedExpressions/should_fold_givenExtendedInfixExpressionWithMinus-root.java";
        String expectedName = "foldingInputs/infixExtendedExpressions/should_fold_givenExtendedInfixExpressionWithMinus.java";
        TestUtils.assertDidFold(this, rootName, expectedName, folder);
    }
}
