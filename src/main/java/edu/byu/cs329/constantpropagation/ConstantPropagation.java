package edu.byu.cs329.constantpropagation;

import edu.byu.cs329.cfg.ControlFlowGraph;
import edu.byu.cs329.cfg.ControlFlowGraphBuilder;
import edu.byu.cs329.constantfolding.ConstantFolding;
import edu.byu.cs329.rd.ReachingDefinitions;
import edu.byu.cs329.rd.ReachingDefinitionsBuilder;
import edu.byu.cs329.rd.ReachingDefinitions.Definition;
import edu.byu.cs329.utils.JavaSourceUtils;
import edu.byu.cs329.utils.TreeModificationUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constant Propagation.
 *
 * @author Eric Mercer
 */
public class ConstantPropagation {

  static final Logger log = LoggerFactory.getLogger(ConstantPropagation.class);
  static final ControlFlowGraphBuilder cfgBuilder = new ControlFlowGraphBuilder();
  static final ReachingDefinitionsBuilder rdBuilder = new ReachingDefinitionsBuilder();

  static class Visitor extends ASTVisitor {
    List<ReachingDefinitions> rdList = null;
    boolean isChanged = false; 

    @Override
    public void endVisit(SimpleName node) {
      ASTNode statement = node.getParent();
      if (statement instanceof Assignment) {
        if (node == ((Assignment) statement).getLeftHandSide()) {
          return;
        }
      }
      for (ReachingDefinitions rd : rdList) {
        if (statement instanceof InfixExpression || statement instanceof PrefixExpression) {
          Statement parent = getStatement(node);
          if (rd.getReachingDefinitions(parent) != null) {
            //all the stuff
            Set<Definition> definitions = rd.getReachingDefinitions(parent);
            Set<Expression> literals = getLiterals(definitions, node);
            // replace simplename with literal and copy subtree
            if (literals.size() == 1) {
              isChanged = true;
              Expression literal = literals.iterator().next();
              replaceChild(literal, node);
            }
            break;
          }
        }
      }
    }

    private void replaceChild(Expression literal, SimpleName node) {
      if (literal instanceof NumberLiteral) {
        String val = ((NumberLiteral) literal).getToken();
        NumberLiteral newLit = node.getAST().newNumberLiteral(val);
        TreeModificationUtils.replaceChildInParent(node, newLit);
      } else if (literal instanceof BooleanLiteral) {
        boolean val = ((BooleanLiteral) literal).booleanValue();
        BooleanLiteral newLit = node.getAST().newBooleanLiteral(val);
        TreeModificationUtils.replaceChildInParent(node, newLit);
      }
    }

    private Statement getStatement(SimpleName node) {
      ASTNode parent = node.getParent();
      while (!(parent instanceof Statement)) {
        parent = parent.getParent();
      }

      return (Statement) parent;
    }

    private Set<Expression> getLiterals(Set<Definition> definitions, SimpleName name) {
      Set<Expression> literals = new HashSet<>();

      for (Definition d : definitions) {
        if (!name.getIdentifier().equals(d.name.getIdentifier())) {
          continue;
        }

        if (d.statement instanceof VariableDeclarationStatement) {
          VariableDeclarationStatement s = (VariableDeclarationStatement) d.statement;
          VariableDeclarationFragment f = (VariableDeclarationFragment) s.fragments().get(0);
          Expression e = f.getInitializer();

          if (e instanceof NumberLiteral || e instanceof BooleanLiteral) {
            literals.add(e);
          }
        } else if (((ExpressionStatement) d.statement).getExpression() instanceof Assignment) {
          ExpressionStatement s = (ExpressionStatement) d.statement;
          Assignment a = (Assignment) s.getExpression();
          Expression e = a.getRightHandSide();

          if (e instanceof NumberLiteral || e instanceof BooleanLiteral) {
            literals.add(e);
          }
        }
      }
      return literals;
    }
  }

  /**
   * Performs constant propagation.
   * 
   * @requires node != null
   * @requires node instanceof CompilationUnit
   *
   * @param node the root node for constant propagation.
   */
  public static void propagate(ASTNode node) {
    boolean didChange = true;

    do {
      ConstantFolding.fold(node);
      List<ControlFlowGraph> cfgList = cfgBuilder.build(node);
      Visitor visitor = new Visitor();
      visitor.rdList = rdBuilder.build(cfgList);
      node.accept(visitor);
      didChange = (ConstantFolding.isChanged() || visitor.isChanged);
    } while (didChange);

    // use visitor
    // for each statement, if has a variable with one declaration, replace variable with literal

    // repeat if node.subtree == (old)node.subtree

  }

  /**
   * Performs constant propagation an a Java file.
   *
   * @param args args[0] is the file to fold and args[1] is where to write the
   *             output
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      log.error("Missing Java input file or output file on command line");
      System.out.println("usage: java DomViewer <java file to parse> <html file to write>");
      return;
    }

    File inputFile = new File(args[0]);
    ASTNode node = JavaSourceUtils.getCompilationUnit(inputFile.toURI());
    ConstantPropagation.propagate(node);

    try {
      PrintWriter writer = new PrintWriter(args[1], "UTF-8");
      writer.print(node.toString());
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
