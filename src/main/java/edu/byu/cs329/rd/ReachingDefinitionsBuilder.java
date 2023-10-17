package edu.byu.cs329.rd;

import edu.byu.cs329.cfg.ControlFlowGraph;
import edu.byu.cs329.rd.ReachingDefinitions.Definition;
import edu.byu.cs329.utils.AstNodePropertiesUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;


/**
 * Builder for reaching definitions on a control flow graph.
 */
public class ReachingDefinitionsBuilder {
  private List<ReachingDefinitions> rdList = null;
  private Map<Statement, Set<Definition>> entrySetMap = null;
  private Map<Statement, Set<Definition>> exitSetMap = null;
  private Map<Statement, Set<Definition>> genSetMap = null;

  /**
   * Computes the reaching definitions for each control flow graph.
   *
   * @param cfgList the list of control flow graphs.
   * @return the coresponding reaching definitions for each graph.
   */
  public List<ReachingDefinitions> build(List<ControlFlowGraph> cfgList) {
    rdList = new ArrayList<ReachingDefinitions>();
    for (ControlFlowGraph cfg : cfgList) {
      ReachingDefinitions rd = computeReachingDefinitions(cfg);
      rdList.add(rd);
    }
    return rdList;
  }

  private ReachingDefinitions computeReachingDefinitions(ControlFlowGraph cfg) {
    exitSetMap = new HashMap<Statement, Set<Definition>>();
    genSetMap = new HashMap<Statement, Set<Definition>>();
    entrySetMap = new HashMap<Statement, Set<Definition>>();
    Set<Definition> parameterDefinitions = createParameterDefinitions(cfg.getMethodDeclaration());
    Statement start = cfg.getStart();
    entrySetMap.put(start, parameterDefinitions);
    
    initialize(start, cfg);
    computeEntrySets(cfg);
    //finish
    
    return new ReachingDefinitions() {
      final Map<Statement, Set<Definition>> reachingDefinitions = 
          Collections.unmodifiableMap(entrySetMap);

      @Override 
      public Set<Definition> getReachingDefinitions(final Statement s) {
        Set<Definition> returnValue = null;
        if (reachingDefinitions.containsKey(s)) {
          returnValue = reachingDefinitions.get(s);
        }
        return returnValue;
      }
    };
  }

  private void initialize(Statement start, ControlFlowGraph cfg) {
    computeExitSets(start, cfg);
    computeGenSets(start, cfg);
  }

  private void computeEntrySets(ControlFlowGraph cfg) {
    Deque<Statement> d = new ArrayDeque<Statement>();
    d.push(cfg.getStart());

    while (!d.isEmpty()) {
      Statement stmt = d.pop();
      Set<Definition> entrySet = unionOverPredecessors(stmt, cfg);
      entrySetMap.put(stmt, entrySet);

      Set<Definition> exitSet = new HashSet<>(entrySet);
      exitSet = kill(stmt, exitSet);
      exitSet.addAll(genSetMap.get(stmt));

      Set<Definition> oldExitSet = getExitSetMapValue(stmt);
      if (oldExitSet != null && exitSet.equals(oldExitSet)) {
        continue;
      }

      exitSetMap.put(stmt, exitSet);
      Set<Statement> succs = cfg.getSuccs(stmt);
      if (succs != null) {
        d.addAll(cfg.getSuccs(stmt));
      }
    }
  }

  private Set<Definition> getExitSetMapValue(Statement statement) {
    if (!exitSetMap.containsKey(statement)) {
      return null;
    }
    return exitSetMap.get(statement);
  }

  private Set<Definition> unionOverPredecessors(Statement statement, ControlFlowGraph cfg) {
    Set<Statement> preds = cfg.getPreds(statement);
    Set<Definition> entrySet = null;
    if (entrySetMap.containsKey(statement)) {
      entrySet = entrySetMap.get(statement);
    } else {
      entrySet = new HashSet<>();
    }

    if (preds != null) {
      for (Statement pred : preds) {
        Set<Definition> exitSetPred = exitSetMap.get(pred);
        entrySet.addAll(exitSetPred);
      }
    }

    return entrySet;
  }
  
  private Set<Definition> kill(Statement statement, Set<Definition> exitSet) {
    if (isRelevantStatement(statement)) {
      SimpleName simpleName = null;
      
      if (statement instanceof VariableDeclarationStatement) {
        simpleName = AstNodePropertiesUtils.getSimpleName((VariableDeclarationStatement) statement);
      } else {
        simpleName = AstNodePropertiesUtils.getSimpleName((Assignment) 
        ((ExpressionStatement) statement).getExpression());
      }

      // comparator problem
      if (exitSet != null) {
        Set<Definition> temp = new HashSet<>();
        temp.addAll(exitSet);
        for (Definition definition : temp) {
          if (simpleName.getIdentifier().equals(definition.name.getIdentifier())) {
            exitSet.remove(definition);
          }
        }
      }
    }
    return exitSet;
  }

  private boolean isRelevantStatement(Statement statement) {
    return (statement instanceof VariableDeclarationStatement 
      || (statement instanceof ExpressionStatement 
      && 
      ((ExpressionStatement) statement).getExpression() instanceof Assignment));
  }

  private void computeGenSets(Statement start, ControlFlowGraph cfg) {
    if (!genSetMap.containsKey(start)) {
      if (start instanceof VariableDeclarationStatement) {
        Definition definition = new Definition();
        definition.name = AstNodePropertiesUtils.getSimpleName(
          (VariableDeclarationStatement) start);
        definition.statement = start;
        genSetMap.put(start, new HashSet<>(Arrays.asList(definition)));
      } else if (start instanceof ExpressionStatement) {
        if (((ExpressionStatement) start).getExpression() instanceof Assignment) {
          Definition definition = new Definition();
          definition.name = AstNodePropertiesUtils.getSimpleName(((Assignment) 
          ((ExpressionStatement) start).getExpression()));
          definition.statement = start;
          genSetMap.put(start, new HashSet<>(Arrays.asList(definition)));
        }
      } else {
        genSetMap.put(start, new HashSet<>());
      }

      Set<Statement> succs = cfg.getSuccs(start);
      if (succs != null) {
        for (Statement succ : succs) {
          computeGenSets(succ, cfg);
        }
      }
    }
  }

  private void computeExitSets(Statement stmt, ControlFlowGraph cfg) {
    if (!exitSetMap.containsKey(stmt)) {
      exitSetMap.put(stmt, new HashSet<>());
      Set<Statement> succs = cfg.getSuccs(stmt);
      if (succs != null) {
        for (Statement succ : succs) {
          computeExitSets(succ, cfg);
        }
      }
    }
  }


  private Set<Definition> createParameterDefinitions(MethodDeclaration methodDeclaration) {
    List<VariableDeclaration> parameterList = 
        getParameterList(methodDeclaration.parameters());
    Set<Definition> set = new HashSet<Definition>();

    for (VariableDeclaration parameter : parameterList) {
      Definition definition = createDefinition(parameter.getName(), null);
      set.add(definition);  
    }

    return set;
  }

  private Definition createDefinition(SimpleName name, Statement statement) {
    Definition definition = new Definition();
    definition.name = name;
    definition.statement = statement;
    return definition;
  }

  private List<VariableDeclaration> getParameterList(Object list) {
    @SuppressWarnings("unchecked")
    List<VariableDeclaration> statementList = (List<VariableDeclaration>) (list);
    return statementList;
  }
}
