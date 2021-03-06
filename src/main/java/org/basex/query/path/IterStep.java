package org.basex.query.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Iterative step expression without numeric predicates.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class IterStep extends AxisStep {
  /**
   * Constructor.
   * @param ii input info
   * @param a axis
   * @param t node test
   * @param p predicates
   */
  IterStep(final InputInfo ii, final Axis a, final Test t, final Expr[] p) {
    super(ii, a, t, p);
  }

  @Override
  public NodeIter iter(final QueryContext ctx) {
    return new NodeIter() {
      AxisIter ai;

      @Override
      public ANode next() throws QueryException {
        if(ai == null) ai = axis.iter(checkNode(ctx));
        while(true) {
          ctx.checkStop();
          final ANode node = ai.next();
          if(node == null) return null;
          // evaluate node test and predicates
          if(test.eq(node) && preds(node, ctx)) return node.finish();
        }
      }

      @Override
      public boolean reset() {
        ai = null;
        return true;
      }
    };
  }
}
