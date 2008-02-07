package org.basex.query.xpath.locpath;

import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeBuilder;

/**
 * Ancestor-or-self Step.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class StepAncOrSelf extends Step {
  @Override
  protected void eval(final Data data, final int p, final NodeBuilder t) {
    int pre = p;

    do {
      final int kind = data.kind(pre);
      test.eval(data, pre, kind, t);
      pre = data.parent(pre, kind);
    } while(pre != -1);
  }

  @Override
  protected void pos(final XPContext ctx, final Data data, final int p)
      throws QueryException {

    int pos = 0;
    int pre = p;

    do {
      final int kind = data.kind(pre);
      if(test.eval(data, pre, kind) && ++pos == posPred) {
        preds.posEval(ctx, pre, result);
        return;
      }
      pre = data.parent(pre, kind);
    } while(pre != -1);
  }

  @Override
  protected void early(final XPContext ctx, final Data data, final int p)
      throws QueryException {

    final int[] pos = new int[preds.size()];
    int pre = p;

    do {
      final int kind = data.kind(pre);
      if(test.eval(data, pre, kind)) {
        if(!preds.earlyEval(ctx, result, pre, pos)) return;
      }
      pre = data.parent(pre, kind);
    } while(pre != -1);
  }
}
