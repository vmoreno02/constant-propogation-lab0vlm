package edu.byu.cs329.constantfolding;

import edu.byu.cs329.utils.ExceptionUtils;
import edu.byu.cs329.utils.TreeModificationUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;

/**
 * Replaces if statements with boolean literal predicates with the accessible branch.
 */
public class IfBooleanLiteralFolding implements Folding {

  class Visitor extends ASTVisitor {
    public boolean didFold = false;

    @Override
    public void endVisit(IfStatement node) {
      Expression exp = node.getExpression();
      if (!(exp instanceof BooleanLiteral)) {
        return;
      }

      boolean val = ((BooleanLiteral) exp).booleanValue();
      ASTNode statement = null;

      if (val) {
        statement = ASTNode.copySubtree(node.getAST(), node.getThenStatement());
      } else {
        statement = ASTNode.copySubtree(node.getAST(), node.getElseStatement());
        if (statement == null) {
          statement = node.getAST().newBlock();
        }
      }

      TreeModificationUtils.replaceChildInParent(node, statement);
      didFold = true;
    }
  }

  /**
  * Replaces if statements with a boolean literal as
  * an operand in the tree with the only reachable statement.
  * 
  * <p>Visits the root and any reachable nodes from the root to replace
  * any IfStatement reachable node containing a boolean literal predicate
  * with the reachable block.
  *
  * <p>top := all nodes reachable from root such that each node 
  *           is an if statement that contains
  *           a boolean predicate
  * 
  * <p>parents := all nodes such that each one is the parent
  *               of some node in top
  * 
  * <p>isFoldable(n) :=    isIfStatement(n)
  *                     /\ isLiteral(getOperand(n)) 
  * 
  * <p>literal(n) := if isLiteral(n) then n else literal(expression(n))
  *
  * <p>branch(n) = reachable branch of if statement
  *
  * @modifies nodes in parents
  * 
  * @requires root != null
  * @requires (root instanceof CompilationUnit) \/ parent(root) != null
  * 
  * @ensures fold(root) == (old(top) != emptyset)
  * @ensures forall n in old(top), exists n' in nodes 
  *             fresh(n')
  *          /\ isBlock(n')
  *          /\ value(n') == value(branch(n))
  *          /\ parent(n') == parent(n)
  *          /\ children(parent(n')) == (children(parent(n)) setminus {n}) union {n'}
  *   
  * @param root the root of the tree to traverse.
  * @return true if if-statement blocks were replaced in the rooted tree
  */
  @Override
  public boolean fold(ASTNode root) {
    checkRequires(root);
    Visitor visitor = new Visitor();
    root.accept(visitor);
    return visitor.didFold;
  }

  private void checkRequires(final ASTNode root) {
    ExceptionUtils.requiresNonNull(root, "Null root passed to PrefixExpressionFolding.fold");
   
    if (!(root instanceof CompilationUnit) && root.getParent() == null) {
      ExceptionUtils.throwRuntimeException(
          "Non-CompilationUnit root with no parent passed to PrefixExpressionFolding.fold"
      );
    }
  }
  
}
