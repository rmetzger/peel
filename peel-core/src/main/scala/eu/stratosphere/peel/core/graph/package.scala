package eu.stratosphere.peel.core

import eu.stratosphere.peel.core.beans.experiment.ExperimentSuite
import eu.stratosphere.peel.core.beans.system.System
import org.slf4j.LoggerFactory

package object graph {

  final val logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Create a directed Graph from all Experiments and their dependencies.
   *
   * @return Graph with systems as vertices and dependencies as edges
   */
  def createGraph(suite: ExperimentSuite): DependencyGraph[Node] = {
    logger.info(s"Constructing dependency graph for suite '${suite.name}'")

    // initial graph
    val g = new DependencyGraph[Node]

    // helper function: process system dependencies
    def processDependencies(s: System): Unit = {
      if (s.dependencies.nonEmpty) {
        for (d <- s.dependencies) {
          g.addEdge(s, d)
          processDependencies(d)
        }
      }
    }

    for (e <- suite.experiments) {
      g.addEdge(suite, e)
      // add the experiment runner
      g.addEdge(e, e.runner)
      processDependencies(e.runner)

      // add the experiment inputs and their dependencies
      for (i <- e.inputs) {
        g.addEdge(e, i)
        // add the input dependencies
        for (x <- i.dependencies) {
          g.addEdge(i, x)
          processDependencies(x)
        }
      }
      // add the experiment outputs and their dependencies
      for (o <- e.outputs) {
        g.addEdge(e, o)
        // add the output dependency
        g.addEdge(o, o.fs)
        processDependencies(o.fs)
      }
    }

    g // return the graph
  }
}
