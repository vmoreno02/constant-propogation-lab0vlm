package edu.byu.cs329.constantfolding;

import edu.byu.cs329.utils.ExceptionUtils;
import edu.byu.cs329.utils.TreeModificationUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;

/**
 * Replaces the bang operator preceding a boolean literal with a boolean literal.
 */
public class PrefixExpressionFolding implements Folding {

  public PrefixExpressionFolding() {}

  class Visitor extends ASTVisitor {
    public boolean didFold = false;

    @Override
    public void endVisit(PrefixExpression node) {
      Operator op = node.getOperator();
      if (op != Operator.NOT) {
        return;
      }

      ASTNode operand = node.getOperand();
      if (!(operand instanceof BooleanLiteral)) {
        return;
      }

      boolean val = ((BooleanLiteral) operand).booleanValue();

      BooleanLiteral newNode = node.getAST().newBooleanLiteral(!val);
      TreeModificationUtils.replaceChildInParent(node, newNode);
      didFold = true;
    }
  }


  /**
  * Replaces prefix expressions preceding literals in the tree with the literals.
  * 
  * <p>Visits the root and any reachable nodes from the root to replace
  * any PrefixExpression reachable node containing a literal
  * with the opposite of the literal itself.
  *
  * <p>top := all nodes reachable from root such that each node 
  *           is a prefix expression that ends
  *           in a literal
  * 
  * <p>parents := all nodes such that each one is the parent
  *               of some node in top
  * 
  * <p>isFoldable(n) :=    isPrefixExpression(n)
  *                     /\ (   isLiteral(expression(n)))
  * 
  * <p>literal(n) := if isLiteral(n) then n else literal(expression(n))
  *
  * @modifies nodes in parents
  * 
  * @requires root != null
  * @requires (root instanceof CompilationUnit) \/ parent(root) != null
  * 
  * @ensures fold(root) == (old(top) != emptyset)
  * @ensures forall n in old(top), exists n' in nodes 
  *             fresh(n')
  *          /\ isLiteral(n')
  *          /\ value(n') == value(!literal(n))
  *          /\ parent(n') == parent(n)
  *          /\ children(parent(n')) == (children(parent(n)) setminus {n}) union {n'}
  *   
  * @param root the root of the tree to traverse.
  * @return true if prefix expressions were replaced in the rooted tree
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
