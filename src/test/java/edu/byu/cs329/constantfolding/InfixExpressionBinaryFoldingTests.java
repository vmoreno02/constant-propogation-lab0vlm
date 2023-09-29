package edu.byu.cs329.constantfolding;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.byu.cs329.TestUtils;

public class InfixExpressionBinaryFoldingTests {
    InfixExpressionBinaryFolding folder = null;

    @BeforeEach
    void beforeEach() {
        folder = new InfixExpressionBinaryFolding();
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
    @DisplayName("Given (3 < 7), when fold, then true")
    void given_3LessThan7_when_fold_then_true() {
        String rootName = "foldingInputs/infixBinaryExpressions/should_fold_givenBinaryExpression-root.java";
        String expectedName = "foldingInputs/infixBinaryExpressions/should_fold_givenBinaryExpression.java";
        TestUtils.assertDidFold(this, rootName, expectedName, folder);
    }

    @Test
    @DisplayName("Given (7 < 3), when fold, then false")
    void given_7LessThan3_when_fold_then_true() {
        String rootName = "foldingInputs/infixBinaryExpressions/should_fold_givenFalseBinaryExpression-root.java";
        String expectedName = "foldingInputs/infixBinaryExpressions/should_fold_givenFalseBinaryExpression.java";
        TestUtils.assertDidFold(this, rootName, expectedName, folder);
    }

    @Test
    @DisplayName("Given no less than expressions, when fold, then no change")
    void given_noLessThanExpressions_when_fold_then_noChange() {
        String rootName = "foldingInputs/infixBinaryExpressions/should_notFold_givenNoBinaryExpressions.java";
        String expectedName = "foldingInputs/infixBinaryExpressions/should_notFold_givenNoBinaryExpressions.java";
        TestUtils.assertDidNotFold(this, rootName, expectedName, folder);
    }
}
