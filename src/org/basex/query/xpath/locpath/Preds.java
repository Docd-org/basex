package org.basex.query.xpath.locpath;

import static org.basex.query.xpath.XPText.*;
import org.basex.data.Serializer;
import org.basex.query.ExprInfo;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.NodeBuilder;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Array;

/**
 * Location Steps.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Preds extends ExprInfo {
  /** Steps array. */
  private Pred[] preds = new Pred[1];
  /** Number of steps. */
  private int size;
  /** Temporary node set. */
  private NodeSet tmp;
  
  /**
   * Returns the number of location steps.
   * @return number of steps
   */
  public int size() {
    return size;
  }

  /**
   * Returns a predicate.
   * @param i predicate offset
   * @return predicate
   */
  public Pred get(final int i) {
    return preds[i];
  }

  /**
   * Adds a predicate.
   * @param pred predicate to be added
   */
  public void add(final Pred pred) {
    if(size == preds.length) preds = Array.extend(preds);
    preds[size++] = pred;
  }
  
  /**
   * Removes the specified step.
   * @param s step index
   */
  public void remove(final int s) {
    Array.move(preds, s + 1, -1, --size - s);
  }

  /**
   * Adds an expression as predicate.
   * @param expr expression to be added
   */
  public void add(final Expr expr) {
    add(new PredSimple(expr));
  }

  /**
   * Adds a position predicate.
   * @param min minimum value
   * @param max maximum value
   */
  public void add(final int min, final int max) {
    add(new PredPos(min, max));
  }

  /**
   * Evaluates the predicates.
   * @param ctx query context
   * @param nodes nodes to be evaluated
   * @param result result nodes
   * @throws QueryException evaluation exception
   */
  public void eval(final XPContext ctx, final NodeBuilder nodes,
      final NodeBuilder result) throws QueryException {

    final NodeSet ns = ctx.local;
    NodeBuilder n = nodes;
    ctx.local = tmp;
    for(int s = 0; s < size; s++) {
      if(n.size == 0) break;
      n = preds[s].eval(ctx, n);
    }
    result.add(n);
    ctx.local = ns;
    nodes.reset();
  }

  /**
   * Early evaluation.
   * @param ctx query context
   * @param result result nodes
   * @param pre pre value
   * @param pos position array
   * @return true if more results are to be expected
   * @throws QueryException evaluation exception
   */
  boolean earlyEval(final XPContext ctx, final NodeBuilder result,
      final int pre, final int[] pos) throws QueryException {

    final NodeSet ns = ctx.local;
    ctx.local = tmp;
    tmp.set(pre);
    tmp.currSize = -1;
    boolean more = true;
    for(int j = 0; j != size; j++) {
      final Pred pred = preds[j];
      if(!pred.eval(ctx, tmp, ++pos[j])) {
        ctx.local = ns;
        return pred.more;
      }
      more &= pred.more;
    }
    result.add(pre);
    ctx.local = ns;
    return more;
  }

  /**
   * Position predicate evaluation.
   * @param ctx query context
   * @param pre pre value
   * @param result result nodes
   * @throws QueryException evaluation exception
   */
  void posEval(final XPContext ctx, final int pre,
      final NodeBuilder result) throws QueryException {

    final NodeSet ns = ctx.local;
    ctx.local = tmp;
    tmp.set(pre);
    tmp.currSize = 1;
    for(int j = 1; j != size; j++) {
      if(!preds[j].eval(ctx, tmp, 1)) {
        ctx.local = ns;
        return;
      }
    }
    ctx.local = ns;
    result.add(pre);
  }

  /**
   * Optimizes the predicates.
   * @param ctx query context
   * @return false if predicate always yields false
   * @throws QueryException evaluation exception
   */
  public boolean compile(final XPContext ctx) throws QueryException {
    tmp = new NodeSet(ctx);
    for(int j = 0; j < size; j++) {
      final Pred pred = preds[j].compile(ctx);
      if(pred.alwaysFalse()) {
        ctx.compInfo(OPTEMPTY);
        size = 0;
        return false;
      }

      if(pred.alwaysTrue()) {
        ctx.compInfo(OPTPRED);
        remove(j);
      } else {
        preds[j] = pred;
      }
    }
    return true;
  }

  /**
   * Checks whether the predicates make use of the position parameter.
   * @return whether position is used
   */
  public boolean usesPos() {
    for(int s = 0; s < size; s++) if(preds[s].usesPos()) return true;
    return false;
  }

  /**
   * Checks whether the predicates make use of the size parameter.
   * @return whether node set size is used
   */
  public boolean usesSetSize() {
    for(int s = 0; s < size; s++) if(preds[s].usesSize()) return true;
    return false;
  }

  /**
   * Returns the predicates for equality.
   * @param cmp predicates to be compared
   * @return result of check
   */
  public boolean sameAs(final Preds cmp) {
    if(size != cmp.size) return false;
    for(int s = 0; s < size; s++) {
      if(!preds[s].sameAs(cmp.preds[s])) return false;
    }
    return true;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    for(int s = 0; s < size; s++) preds[s].plan(ser);
  }

  @Override
  public String color() {
    return "FF3333";
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(int s = 0; s < size; s++) sb.append(preds[s].toString());
    return sb.toString();
  }
}
