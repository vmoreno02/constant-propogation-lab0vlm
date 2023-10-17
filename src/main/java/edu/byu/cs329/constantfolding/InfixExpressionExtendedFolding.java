package edu.byu.cs329.constantfolding;

import edu.byu.cs329.utils.ExceptionUtils;
import edu.byu.cs329.utils.TreeModificationUtils;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.NumberLiteral;

/**
 * Replaces infix expressions that add number literals with the sum.
 */
public class InfixExpressionExtendedFolding implements Folding {

  class Visitor extends ASTVisitor {
    public boolean didFold = false;

    @Override
    public void endVisit(InfixExpression node) {
      Operator operator = node.getOperator();
      if (operator != Operator.PLUS) {
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

      @SuppressWarnings("unchecked")
      List<ASTNode> extended = node.extendedOperands();

      int sumOperands = leftVal + rightVal;
      for (int i = 0; i < extended.size(); i++) {
        sumOperands += Integer.parseInt(((NumberLiteral) extended.get(i)).getToken());
      }

      NumberLiteral newNode = node.getAST().newNumberLiteral(Integer.toString(sumOperands));
      TreeModificationUtils.replaceChildInParent(node, newNode);
      didFold = true;
    }
  }

  /**
  * Replaces infix expressions with multiple added number literals as
  * operands in the tree with the sum.
  * 
  * <p>Visits the root and any reachable nodes from the root to replace
  * any InfixExpression reachable node containing a two number literals
  * with the evaluation of the infix expression.
  *
  * <p>top := all nodes reachable from root such that each node 
  *           is an infix expression that contains
  *           at least two added literals
  * 
  * <p>parents := all nodes such that each one is the parent
  *               of some node in top
  * 
  * <p>isFoldable(n) :=    isInfixExpression(n)
  *                     /\ isLiteral(getLeftOperand(n)) 
  *                     /\ isLiteral(getRightOperand(n))
  *                     /\ (forall i in getExtendedOperators(n): isLiteral(i))
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
  * @return true if summation infix expressions were replaced in the rooted tree
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
