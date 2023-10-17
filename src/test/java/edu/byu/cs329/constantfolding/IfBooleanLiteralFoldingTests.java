package edu.byu.cs329.constantfolding;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.byu.cs329.TestUtils;

@DisplayName("Tests for IfBooleanLiteralFolding")
public class IfBooleanLiteralFoldingTests {
    IfBooleanLiteralFolding folder = null;

    @BeforeEach
    void beforeEach() {
        folder = new IfBooleanLiteralFolding();
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

    // @Test
    // @DisplayName("Given true block, when fold, then true statement")
    // void given_trueBlock_when_fold_then_trueStatement() {
    //     String rootName = "foldingInputs/ifStatement/should_fold_givenTrueIfStatement-root.java";
    //     String expectedName = "foldingInputs/ifStatement/should_fold_givenTrueIfStatement.java";
    //     TestUtils.assertDidFold(this, rootName, expectedName, folder);
    // }

    @Test
    @DisplayName("Given false block, when fold, then true statement")
    void given_falseBlock_when_fold_then_trueStatement() {
        String rootName = "foldingInputs/ifStatement/should_fold_givenFalseIfStatement-root.java";
        String expectedName = "foldingInputs/ifStatement/should_fold_givenFalseIfStatement.java";
        TestUtils.assertDidFold(this, rootName, expectedName, folder);
    }

    @Test
    @DisplayName("Given only false, when fold, then empty statement")
    void given_falseBlock_when_fold_then_emptyStatement() {
        String rootName = "foldingInputs/ifStatement/should_fold_givenOnlyFalse-root.java";
        String expectedName = "foldingInputs/ifStatement/should_fold_givenOnlyFalse.java";
        TestUtils.assertDidFold(this, rootName, expectedName, folder);
    }

    // @Test
    // @DisplayName("Given else if block, when fold, then no change")
    // void given_elseIfBlock_when_fold_then_noChange() {
    //     String rootName = "foldingInputs/ifStatement/should_notFold_givenIfElseBlock.java";
    //     String expectedName = "foldingInputs/ifStatement/should_notFold_givenIfElseBlock.java";
    //     TestUtils.assertDidNotFold(this, rootName, expectedName, folder);
    // }

    @Test
    @DisplayName("Given no if blocks, when fold, then no change")
    void given_noIfBlocks_when_fold_then_noChange() {
        String rootName = "foldingInputs/ifStatement/should_notFold_givenNoIfLiterals.java";
        String expectedName = "foldingInputs/ifStatement/should_notFold_givenNoIfLiterals.java";
        TestUtils.assertDidNotFold(this, rootName, expectedName, folder);
    }
}
