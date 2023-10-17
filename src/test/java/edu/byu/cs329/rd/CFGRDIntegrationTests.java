package edu.byu.cs329.rd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import edu.byu.cs329.TestUtils;
import edu.byu.cs329.cfg.ControlFlowGraph;
import edu.byu.cs329.cfg.ControlFlowGraphBuilder;
import edu.byu.cs329.cfg.StatementTracker;
import edu.byu.cs329.rd.ReachingDefinitions.Definition;

public class CFGRDIntegrationTests {
    ControlFlowGraph cfg = null;
    ControlFlowGraphBuilder cfgBuilder = null;
    List<ControlFlowGraph> cfgList = null;
    ReachingDefinitionsBuilder rdBuilder = null;
    StatementTracker statementTracker = null;

    @BeforeEach
    void beforeEach() {
      cfgBuilder = new ControlFlowGraphBuilder();
      rdBuilder = new ReachingDefinitionsBuilder();
    }

    @Test
    @Tag("Integration")
    @DisplayName("Given one function, when cfg and rd build, then one cfg and one definition")
    void given_oneFunction_when_cfgAndRdBuild_then_oneCFGAndOneDefinition() {
      init("cfgInputs/should_ReturnOneCFG_when_OneFunction.java");
      assertEquals(1, cfgList.size());
      ReachingDefinitions rd = getReachingDefinitions(cfg);
      Set<Definition> definitions = rd.getReachingDefinitions(statementTracker.getReturnStatement(0));
      assertEquals(1, definitions.size());
    }

    @Test
    @Tag("Integration")
    @DisplayName("Given two functions, when cfg and rd build, then two cfgs")
    void given_twoFunctions_when_cfgAndRdBuild_then_twoCFGs() {
      init("cfgInputs/should_ReturnTwoCFGs_when_TwoMethods.java");
      assertEquals(2, cfgList.size());
      ReachingDefinitions rd1 = getReachingDefinitions(cfgList.get(0));
      ReachingDefinitions rd2 = getReachingDefinitions(cfgList.get(1));
      Set<Definition> d1 = rd1.getReachingDefinitions(statementTracker.getReturnStatement(0));
      Set<Definition> d2 = rd2.getReachingDefinitions(statementTracker.getVariableDeclarationStatement(1));
      assertAll(
        () -> assertEquals(1, d1.size()),
        () -> assertEquals(1, d2.size()),
        () -> assertTrue(doesDefine("x", d1)),
        () -> assertTrue(doesDefine("y", d2))
      );
    }

    void init(String fileName) {
      ControlFlowGraphBuilder cfgBuilder = new ControlFlowGraphBuilder();
      ASTNode node = TestUtils.getASTNodeFor(this, fileName);
      cfgList = cfgBuilder.build(node);
      cfg = cfgList.get(0);
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
    List<ReachingDefinitions> reachingDefinitionsList = rdBuilder.build(list);
    assertEquals(1, reachingDefinitionsList.size());
    return reachingDefinitionsList.get(0);
  }
}
