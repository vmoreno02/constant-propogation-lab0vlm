package edu.byu.cs329.rd;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.Assignment.Operator;

import edu.byu.cs329.cfg.ControlFlowGraph;

public class MockUtils {
  public static ControlFlowGraph newMockForEmptyMethodWithTwoParameters(String first, String second) {
    ControlFlowGraph cfg = mock(ControlFlowGraph.class);
    Statement statement = mock(Statement.class);
    when(cfg.getStart()).thenReturn(statement);
    MethodDeclaration methodDeclarion = mock(MethodDeclaration.class);
    VariableDeclaration firstParameter = newMockForVariableDeclaration(first);
    VariableDeclaration secondParameter = newMockForVariableDeclaration(second);
    List<VariableDeclaration> parameterList = new ArrayList<VariableDeclaration>();
    parameterList.add(firstParameter);
    parameterList.add(secondParameter);
    when(methodDeclarion.parameters()).thenReturn(parameterList);
    when(cfg.getMethodDeclaration()).thenReturn(methodDeclarion);
    return cfg;
  }

  public static VariableDeclaration newMockForVariableDeclaration(String name) {
    VariableDeclaration declaration = mock(VariableDeclaration.class);
    SimpleName simpleName = mock(SimpleName.class);
    when(simpleName.getIdentifier()).thenReturn(name);
    when(declaration.getName()).thenReturn(simpleName);
    return declaration;
  }

  public static ControlFlowGraph newMockForSimpleGraph(String name) {
    ControlFlowGraph cfg = mock(ControlFlowGraph.class);
    MethodDeclaration methodDeclarion = mock(MethodDeclaration.class);
    VariableDeclarationStatement varDec = newMockForVariableDeclarationStatement(name);
    Statement end = mock(Statement.class);

    Set<Statement> preds = new HashSet<>(Arrays.asList(varDec));
    Set<Statement> succs = new HashSet<>(Arrays.asList(end));

    when(cfg.getMethodDeclaration()).thenReturn(methodDeclarion);
    when(cfg.getStart()).thenReturn(varDec);
    when(cfg.getEnd()).thenReturn(end);
    when(cfg.getPreds(end)).thenReturn(preds);
    when(cfg.getSuccs(varDec)).thenReturn(succs);
    return cfg;
  }

  public static VariableDeclarationStatement newMockForVariableDeclarationStatement(String name) {
    VariableDeclarationStatement statement = mock(VariableDeclarationStatement.class);
    VariableDeclarationFragment fragment = mock(VariableDeclarationFragment.class);
    SimpleName simpleName = mock(SimpleName.class);
    List<VariableDeclarationFragment> fragments = new ArrayList<VariableDeclarationFragment>();
    fragments.add(fragment);
    when(simpleName.getIdentifier()).thenReturn(name);
    when(fragment.getName()).thenReturn(simpleName);
    when(statement.fragments()).thenReturn(fragments);
    return statement;
  }

  public static ControlFlowGraph newMockForIfStatementGraph(String nameTrue, int valTrue, String nameFalse, int valFalse) {
    ControlFlowGraph cfg = mock(ControlFlowGraph.class);
    VariableDeclarationStatement start = newMockForVariableDeclarationStatement(nameTrue);
    Statement end = mock(Statement.class);
    MethodDeclaration methodDeclarion = mock(MethodDeclaration.class);
    ExpressionStatement exp1 = newMockForExpressionStatement(nameTrue, valTrue, Operator.ASSIGN);
    ExpressionStatement exp2 = newMockForExpressionStatement(nameFalse, valFalse, Operator.ASSIGN);

    Set<Statement> startSuccs = new HashSet<>(Arrays.asList(exp1, exp2));
    Set<Statement> ifPreds = new HashSet<>(Arrays.asList(start));
    Set<Statement> ifSuccs = new HashSet<>(Arrays.asList(end));
    
    when(cfg.getMethodDeclaration()).thenReturn(methodDeclarion);
    when(cfg.getStart()).thenReturn(start);
    when(cfg.getEnd()).thenReturn(end);
    when(cfg.getSuccs(start)).thenReturn(startSuccs);
    when(cfg.getPreds(exp1)).thenReturn(ifPreds);
    when(cfg.getPreds(exp2)).thenReturn(ifPreds);
    when(cfg.getSuccs(exp1)).thenReturn(ifSuccs);
    when(cfg.getSuccs(exp2)).thenReturn(ifSuccs);
    when(cfg.getPreds(end)).thenReturn(startSuccs);
    return cfg;
  }

  public static ControlFlowGraph newMockForWhileStatementGraph(String name, int val) {
    ControlFlowGraph cfg = mock(ControlFlowGraph.class);
    VariableDeclarationStatement start = newMockForVariableDeclarationStatement(name);
    Statement end = mock(Statement.class);
    MethodDeclaration methodDeclarion = mock(MethodDeclaration.class);
    ExpressionStatement whileStatement = newMockForExpressionStatement(name, val, Operator.PLUS_ASSIGN);

    Set<Statement> startSuccs = new HashSet<>(Arrays.asList(whileStatement, end));
    Set<Statement> whilePredsSuccs = new HashSet<>(Arrays.asList(start));

    when(cfg.getMethodDeclaration()).thenReturn(methodDeclarion);
    when(cfg.getStart()).thenReturn(start);
    when(cfg.getEnd()).thenReturn(end);
    when(cfg.getSuccs(start)).thenReturn(startSuccs);
    when(cfg.getPreds(whileStatement)).thenReturn(whilePredsSuccs);
    when(cfg.getSuccs(whileStatement)).thenReturn(whilePredsSuccs);
    when(cfg.getPreds(end)).thenReturn(whilePredsSuccs);
    return cfg;
  }

  public static ExpressionStatement newMockForExpressionStatement(String name, int value, Operator operator) {
    ExpressionStatement statement = mock(ExpressionStatement.class);
    Assignment assignment = mock(Assignment.class);
    SimpleName simpleName = mock(SimpleName.class);
    NumberLiteral numberLiteral = mock(NumberLiteral.class);

    when(statement.getExpression()).thenReturn(assignment);
    when(assignment.getOperator()).thenReturn(operator);
    when(assignment.getLeftHandSide()).thenReturn(simpleName);
    when(assignment.getRightHandSide()).thenReturn(numberLiteral);
    when(simpleName.getIdentifier()).thenReturn(name);
    when(numberLiteral.getToken()).thenReturn(Integer.toString(value));
    return statement;
  }
}
