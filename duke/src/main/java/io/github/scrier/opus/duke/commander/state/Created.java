package io.github.scrier.opus.duke.commander.state;

import io.github.scrier.opus.duke.commander.ClusterDistributorProcedure;

/**
 * State handling for Created transactions.
 * @author andreas.joelsson
 * {@code * -> ABORTED}
 */
public class Created extends State {

	public Created(ClusterDistributorProcedure parent) {
	  super(parent);
  }

}
