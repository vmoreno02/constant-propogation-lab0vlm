package edu.byu.cs329.rd;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.byu.cs329.TestUtils;
import edu.byu.cs329.cfg.ControlFlowGraph;
import edu.byu.cs329.cfg.ControlFlowGraphBuilder;
import edu.byu.cs329.cfg.StatementTracker;
import edu.byu.cs329.rd.ReachingDefinitions.Definition;

@DisplayName("Tests for ReachingDefinitionsBuilder")
public class ReachingDefinitionsBuilderTests {

  ReachingDefinitionsBuilder unitUnderTest = null;
  ControlFlowGraph controlFlowGraph = null;
  StatementTracker statementTracker = null;

  @BeforeEach
  void beforeEach() {
    unitUnderTest = new ReachingDefinitionsBuilder();
  }

  @Test
  @Tag("Parameters")
  @DisplayName("Should have a definition for each parameter at start when the method declaration has parameters.")
  void should_HaveDefinitionForEachParameterAtStart_when_MethodDeclarationHasParameters() {
    ControlFlowGraph controlFlowGraph = MockUtils.newMockForEmptyMethodWithTwoParameters("a", "b");
    ReachingDefinitions reachingDefinitions = getReachingDefinitions(controlFlowGraph);
    Statement start = controlFlowGraph.getStart();
    Set<Definition> definitions = reachingDefinitions.getReachingDefinitions(start);
    assertEquals(2, definitions.size());
    assertAll("Parameters Defined at Start", 
        () -> assertTrue(doesDefine("a", definitions)),
        () -> assertTrue(doesDefine("b", definitions))
    );
  }

  @Test
  @Tag("Shapes")
  @DisplayName("Given straight line, when build, then entry set of return is var dec")
  void given_straightLine_when_build_then_entrySetOfReturnIsVarDec() {
    ControlFlowGraph cfg = MockUtils.newMockForSimpleGraph("x");
    ReachingDefinitions reachingDefinitions = getReachingDefinitions(cfg);
    Statement end = cfg.getEnd();
    Set<Definition> definitions = reachingDefinitions.getReachingDefinitions(end);
    assertEquals(1, definitions.size());
    assertTrue(doesDefine("x", definitions));
  }

  @Test
  @Tag("Shapes")
  @DisplayName("Given if statement, when build, then entry set of return is both branches")
  void given_ifStatement_when_build_then_entrySetOfReturnIsBothBranches() {
    ControlFlowGraph cfg = MockUtils.newMockForIfStatementGraph("x", 2, "y", 5);
    ReachingDefinitions reachingDefinitions = getReachingDefinitions(cfg);
    Statement end = cfg.getEnd();
    Set<Definition> definitions = reachingDefinitions.getReachingDefinitions(end);
    // first variable is a vardec, so there are 3 definitions
    assertEquals(3, definitions.size());
    assertAll( 
        () -> assertTrue(doesDefine("x", definitions)),
        () -> assertTrue(doesDefine("y", definitions))
    );
  }

  @Test
  @Tag("Shapes")
  @DisplayName("Given loop, when build, then entry set of return is first loop statement")
  void given_loop_when_build_then_entrySetOfReturnIsFirstLoopStatement() {
    ControlFlowGraph cfg = MockUtils.newMockForWhileStatementGraph("x", 2);
    ReachingDefinitions reachingDefinitions = getReachingDefinitions(cfg);
    Statement end = cfg.getEnd();
    Set<Definition> definitions = reachingDefinitions.getReachingDefinitions(end);
    assertEquals(1, definitions.size());
    assertTrue(doesDefine("x", definitions));
  }

  @Test
  @Tag("Shapes")
  @DisplayName("Given loop, when build, then get preds called on loop statement twice")
  void given_loop_when_build_then_getPredsCalledOnLoopStatementTwice() {
    init("cfgInputs/should_CheckMultipleTimes_when_Loop.java");
    ControlFlowGraph spy = Mockito.spy(controlFlowGraph);
    getReachingDefinitions(spy);
    Statement whileStatement = statementTracker.getWhileStatement(0);
    Mockito.verify(spy, times(2)).getPreds(whileStatement);
  }

  void init(String fileName) {
    ControlFlowGraphBuilder cfgBuilder = new ControlFlowGraphBuilder();
    ASTNode node = TestUtils.getASTNodeFor(this, fileName);
    List<ControlFlowGraph> cfgList = cfgBuilder.build(node);
    assertEquals(1, cfgList.size());
    controlFlowGraph = cfgList.get(0);
    statementTracker = new StatementTracker(node);
  }

  private boolean doesDefine(String name, final Set<Definition> definitions) {
    for (Definition definition : definitions) {
      if (definition.name.getIdentifier().equals(name)) {
        return true;
      }
    }
    return false;
  }

  private ReachingDefinitions getReachingDefinitions(ControlFlowGraph controlFlowGraph) {
    List<ControlFlowGraph> list = new ArrayList<ControlFlowGraph>();
    list.add(controlFlowGraph);
    List<ReachingDefinitions> reachingDefinitionsList = unitUnderTest.build(list);
    assertEquals(1, reachingDefinitionsList.size());
    return reachingDefinitionsList.get(0);
  }
}
