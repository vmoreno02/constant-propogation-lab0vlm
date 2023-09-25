package edu.byu.cs329;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import edu.byu.cs329.constantfolding.Folding;
import edu.byu.cs329.utils.JavaSourceUtils;
import java.net.URI;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils extends JavaSourceUtils {
  static Logger log = LoggerFactory.getLogger(TestUtils.class);
  
  public static ASTNode getASTNodeFor(final Object t, String name){
    URI uri = JavaSourceUtils.getUri(t, name);
    assertNotNull(uri);
    ASTNode root = JavaSourceUtils.getCompilationUnit(uri);
    return root;
  }

  public static void assertDidFold(final Object t, String rootName, String expectedName, Folding folderUnderTest) {
    ASTNode root = getASTNodeFor(t, rootName);
    Boolean didFold = folderUnderTest.fold(root);
    log.debug(root.toString());
    assertTrue(didFold);
    ASTNode expected = getASTNodeFor(t, expectedName);
    assertTrue(expected.subtreeMatch(new ASTMatcher(), root));
  }

  public static void assertDidNotFold(final Object t, String rootName, String expectedName, Folding folderUnderTest) {
    ASTNode root = getASTNodeFor(t, rootName);  
    assertFalse(folderUnderTest.fold(root));
    ASTNode expected = getASTNodeFor(t, expectedName);
    assertTrue(expected.subtreeMatch(new ASTMatcher(), root));
  }
}
