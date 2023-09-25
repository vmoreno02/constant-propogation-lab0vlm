# Part 2: Control Flow Graph and Reaching Definitions

**Be sure to merge the pull-request from part 1** into the mainline branch before starting part 2. Part 2 should be on a **new feature branch** from the mainline at a point that includes part 1.

For this part of the project, you will be modifying and adding code to the `edu.byu.cs329.cfg` and `edu.byu.cs329.rd` packages.

This assignment's first objective is to write tests for the `ControlFlowGraphBuilder` class inside the `edu.byu.cs329.cfg` package that builds the control flow graph for each `MethodDeclaration` in a `CompilationUnit`. The graph is constructed with an `ASTVisitor` using the algorithm defined in the [lecture notes](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/cfg-rd-lecture.md). The tests should be written from the specification using black-box techniques.

The second objective is to implement and test the reaching definitions data-flow analysis as defined in the [lecture notes](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/cfg-rd-lecture.md) on a `ControlFlowGraph`. In other words, given a control flow graph for a method, compute the reaching definitions for that graph and black-box test the implementation that builds the `ReachingDefinitions` instances. The test for the reaching definitions analysis should use mocking for the control flow graphs so that the tests are independent of any implementation that builds the control flow graphs.

Please keep in mind that the next part will integrate constant folding, control flow graphs, and reaching definitions to implement constant propagation.

## Reading

See [cfg-rd-lecture.md](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/cfg-rd-lecture.md).

## Java Subset

Use the same subset of Java as before and as defined in the [Part 1 Constant Folding](part1-constant-folding).

## Interfaces

The `ControlFlowGraph` and `ReachingDefinitions` are interfaces for how to interact with the objects. For example, reaching definitions takes a control flow graph and computes the reaching definitions for each statement in that graph. Later, when constant propagation is implemented, for any given statement in the control flow graph, it will check to see how many definitions reach that statement for a given variable, and if there is just one definition, and that definition is to a literal, then the variable is replaced with the literal.

```java
x = 5; // statement s0
y = x + 4; // statement s1
```

In the above example, assuming that ```reachDefs``` is an instance of something that implements the ```ReachingDefinitions``` interface, then the reaching definitions for statement 1 ```reachDefs.getReachingDefinitions(s1)``` should return the set ```{(x, s0)}```. Notice that the set contains the name of the variable and the statement in the control flow graph that defines that variable. This pair of name and statement exactly match the pairs that are in the entry-sets and exit-sets for the analysis. In this way, ```reachDefs.getReachingDefinitions(s1)``` returns the entry set for statement ```s1``` from the reaching definitions analysis. Any implementation of the interface will need to compute from the control flow graph for a method declaration the entry-sets in order to implement the interface. There should be an instance of the ```ReachingDefinitions``` implementation for each ```MethodDeclaration``` in the input program.

## Assignment Requirements

  1. Update the project POM file to allow the `mvn exec:java` command to run tests in the `edu.byu.cs329.cfg` and `edu.byu.cs329.rd` packages as explained under the **Testing** section below.
  2. Write tests in `ControlFlowGraphBuilderTests` for `IfStatement` and fix any discovered defects. Use its for guidance. There should only be around 3-5 additional tests. Follow the test approach in the existing given tests for `MethodDeclaration` and `Block`.
  3. Write a minimal set of tests for `ReachingDefinitionsBuilder` given a list with a single `ControlFlowGraph`. The tests should validate that the algorithm is correctly implemented. There is no formal specification for guiding black-box test generation. Reason over shapes of control-flow graph structures and **only test interesting shapes**. There should be less than a handful (e.g. 3 to 6) of tests to cover all the **interesting shapes**.
  4. Implement the reaching definitions algorithm in `ReachingDefinitionsBuilder`. Be sure all tests pass. Add any additional tests as appropriate.
  
**Further requirements for (3)**

  1. At least one test should fully mock the input `ControlFlowGraph` and use the `Mockito.verify` interface with the mock to validate the algorithm.
  2. At least one test should spy the input and use the `Mockito.verify` interface with the spy to validate the algorithm; the `ControlFlowGraphBuilder` should be used to create the object to spy.
  3. The remaining tests should use the `ControlFlowGraphBuilder` and directly check the resulting instance of `ReachingDefinitions`

Optional, **not required**, activities for practice:

  * Write tests in `ControlFlowGraphBuilderTests` for `ReturnStatement` and `WhileStatement`.

## What to turn in?

When you are done with this assignment, create a pull request of your feature branch containing the solution. Upon submission of your pull request, GitHub will give you a sanity check by running Maven commands that the TA would have run to grade your assignment. Note that passing the GitHub build *does not* mean that you will get full credit for the assignment.

Submit to Canvas the URL of the pull request.

## Testing

The easiest way to run tests is with `mvn test`. See [README.md](README.md) for details on how to select specific test classes and test cases.

### Running tests with `mvn exec:java`

See [README.md](README.md) for details on how to select specific test classes and test cases with `mvn exec:java`. 

The `POM Notes on testing` section of [the README.md](README.md) explains how the POM file configures the `mvn exec:java` command and about JUnit's console launcher. Review that section and replace the following line in the POM file if desired:

```xml
    <argument>--include-package=edu.byu.cs329.constantfolding</argument>
```

with this:

```xml
    <argument>--include-package=edu.byu.cs329.cfg</argument>
    <argument>--include-package=edu.byu.cs329.rd</argument>
```

After making the changes and rebuilding the project with a command like `mvn clean test`, run `mvn exec:java`. You should see that the tests from those packages run instead of the tests in the `edu.byu.cs329.constantfolding` package.

### Test Framework for Control Flow Graphs

The tests for the `ControlFlowGraphBuilder` use the `StatementTracker` to create a list of each type of statement in a compilation unit. The statements in each type of list are ordered by visit order. The `StatementTracker` is a convenient and easy way to get the `ASTNode` instance for any statement for checking specific edges between statements in the particular method from which the `ControlFlowGraph` is constructed. An example of using the `StatementTracker` to get statements checking specific edges is shown in the tests for `Block`. Intuitively though, the `StatementTracker` makes it easy to get any particular statement in the method to check for the presence or absence of an edge.

### Understanding the specifications

The specification is written to mimic the formal definition of the algorithm in [cfg-rd-lecture.md](https://bitbucket.org/byucs329/byu-cs-329-lecture-notes/src/master/cfg-rd-lecture.md). It relies on several helper definitions:

  * `S` is a list of statements that belong to a `Block` such as the list of statements in the block for the method declaration
  * `s` is a statement sometimes with an index in a list as a subscript
  * `last(S)` is the last statement in the list and undefined for an empty list
  * `first(S)` is the first statement in the list and undefined when the list is empty
  * `defined(first(S))` and `defined(last(S))` true if the thing is defined and false otherwise
  * `firstReturn(S)` is the index of the first return statement in the list and when there is no return the index of the last statement in the list

### Updating Test Inputs in the Resources Directory

Anytime a test input is changed, then a `mvn compile` may be required to update the `target` directory with with the changed resource. Be aware of this quirk if ever the test input file has been modified but the test is not using the modified input.

### Mocks Mocks and More Mocks

Creating the mocks for the `ControlFlowGraph` instances is tricky: one because it can be tedious, and two because it requires also creating mocks for several different `ASTNode` types:

* `VariableDeclaration`
* `VariableDeclarationStatement`
* `Assignment`
* `ExpressionStatement`
* `SimpleName`
* `MethodDeclaration`
* `Block`

The above list is not comprehensive as it depends somewhat on the shape of the graphs being mocked, but it is a good starting point.

The goal is to only define required `when().thenReturn()` behavior for each mock. And it is more common than uncommon to be returning mocks for that behavior since an `ASTNode` is likely to use mocks in its own mock. Following is some brief discussion for the less obvious `ASTNodes` to help get started.

The `ControlFlowGraph` itself needs to mock its entire interface. That includes the behavior for `ControlFlowGraph.getSuccs(Statement)` and `ControlFlowGraph.getPreds(Statement)` for each statement in the graph. Drawing the graph being mocked is helpful to be sure all the edges are present. Recall that `ControlFlowGraph.getEnd()` returns the `Block` in the method declaration (see `ControlFlowGraphBuilder#Visitor.visit(MethodDeclaration)`). Using the block in the method declaration for the end is convenient so that every return statement goes to the same place. Remember, it is not required to look in the block as the control flow graph defines edges between statements. It is necessary to look at each statement though to see if it generates a definition (e.g., `VariableDeclarationStatement` and `ExpressionStatement` in the case of it wrapping an `Assignment`).

#### Variable Declaration Statement

The `VariableDeclarationStatement` has a list of *fragments* where the actual definitions happen, `VariableDeclarationStatement.fragments()`. It returns a `List<VariableDeclaration>` instance. A fragment is a `VariableDeclaration`. It needs a `SimpleName` to return for `VariableDeclaration.getName()`.

#### Expression Statement

An `ExpressionStatement` wraps an `Expression` that it returns with `ExpressionStatement.getExpression()`. The interesting expression in terms of a reaching definitions analysis is `Assignment`. The `Assignment` needs an expressions for the left-hand side with `Assignment.getLeftHandSide()`. That expression should be a `SimpleName` for this project.

## Other Things to Consider

  * Creating the mocks for the control flow graph is error prone. It is not unusual to find that the reaching definitions implementation is fine and rather the test failed because the mock was not correct.
  * `doesDefine` in `ReachingDefinitionsBuilderTests` is easily modified to check not just for a name but for the mock that is the expected statement for the definition.
  * Be sure when computing the union over predecessors that there is special care taken for the start node so that it includes the definitions for the parameters.
  * Compute once and save the definitions in a `GenSet` map so that the same instances of `Definition` are used through all the analysis. Using the **same instances** everywhere means that two sets of definitions can be compared for equality without having to define an `equals` method for `Definition` types (e.g. `set1.equals(set2)` works as expected).
  * `SimpleName.getIdentifier()` gives the name and is what needs to be mocked.
  * Write code to create mocks for things such as `VariableDeclaration`, `VariableDeclarationStatement`, `Assignment`, `ExpressionStatement`, etc.
  * The code for the reaching definitions analysis should be simple and may prove to be less code than the test code to build the mocks and define the tests.

## Rubric

| Item | Point Value |
| ------- | ----------- |
| Minimal number of tests for `IfStatement` in ```ControlFlowGraphBuilder``` using black-box testing | 50 |
| Minimal number of tests for ```ReachingDefinitionsBuilder``` using black-box testing and mocks for the `ControlFlowGraph` | 75 |
| Consistent, readable, and descriptive grouping and naming of all tests using `@Nested` or `@Tag` along with `@DisplayName`  | 10 |
| Implementation of ```ReachingDefinitions``` | 40 |
| Adherence to best practices (e.g., no errors, no warnings, documented code, well grouped commits, appropriate commit messages, etc.) | 10 | |
| System integration test | 15 | |
