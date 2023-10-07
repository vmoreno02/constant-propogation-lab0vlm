package edu.byu.cs329.constantfolding;

import edu.byu.cs329.utils.ExceptionUtils;
import edu.byu.cs329.utils.TreeModificationUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.NumberLiteral;

/**
 * Replaces "less than" infix expressions with number literals with a boolean literal.
 */
public class InfixExpressionBinaryFolding implements Folding {

  class Visitor extends ASTVisitor {
    public boolean didFold = false;

    @Override
    public void endVisit(InfixExpression node) {
      Operator operator = node.getOperator();
      if (operator != Operator.LESS) {
        return;
      }

      ASTNode leftOp = node.getLeftOperand();
      if (!(leftOp instanceof NumberLiteral)) {
        return;
      }
      int leftVal = Integer.parseInt(((NumberLiteral) leftOp).getToken());

      ASTNode rightOp = node.getRightOperand();
      if (!(rightOp instanceof NumberLiteral)) {
        return;
      }
      int rightVal = Integer.parseInt(((NumberLiteral) rightOp).getToken());

      BooleanLiteral newNode = node.getAST().newBooleanLiteral(leftVal < rightVal);
      TreeModificationUtils.replaceChildInParent(node, newNode);
      didFold = true;
    }
  }

  /**
  * Replaces infix expressions with two number literals as
  * operands in the tree with the boolean literals.
  * 
  * <p>Visits the root and any reachable nodes from the root to replace
  * any InfixExpression reachable node containing a two number literals
  * with the evaluation of the infix expression.
  *
  * <p>top := all nodes reachable from root such that each node 
  *           is an infix expression that contains
  *           two literals
  * 
  * <p>parents := all nodes such that each one is the parent
  *               of some node in top
  * 
  * <p>isFoldable(n) :=    isInfixExpression(n)
  *                     /\ isLiteral(getLeftOperand(n)) 
  *                     /\ isLiteral(getRightOperand(n))
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
  *          /\ value(n') == value(eval(n))
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
