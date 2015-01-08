package io.github.scrier.opus.duke.commander.state;

import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;

/**
 * State handling for Aborted transactions.
 * @author andreas.joelsson
 * {@code * -> ABORTED}
 */
public class Aborted extends State {

	public Aborted(ClusterDistributorProcedure parent) {
	  super(parent);
  }

}
