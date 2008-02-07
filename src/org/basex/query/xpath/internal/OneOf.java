package org.basex.query.xpath.internal;

import static org.basex.query.xpath.XPText.*;
import org.basex.data.Serializer;
import org.basex.index.Index;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.Path;
import org.basex.query.xpath.expr.Union;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.Comp;
import org.basex.query.xpath.values.Literal;
import org.basex.query.xpath.values.Item;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This Expression assembles a number of similar comparisons of which one
 * has to evluate true.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class OneOf extends InternalExpr {
  /** First Expression. */
  final LocPath path;
  /** Second Expression. */
  final Item[] vals;
  /** Comparator. */
  final Comp cmp;

  /**
   * Constructor.
   * @param p location path
   * @param v second expression to compare with first
   * @param c comparator
   */
  public OneOf(final LocPath p, final Item[] v, final Comp c) {
    path = p;
    vals = v;
    cmp = c;
  }

  @Override
  public Bool eval(final XPContext ctx)
      throws QueryException {

    final Item v = ctx.eval(path);
    if(v.size() == 0) return Bool.FALSE;

    for(final Item val : vals) {
      if(Comp.EQ.eval(v, val)) return Bool.TRUE;
    }
    return Bool.FALSE;
  }

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr) {
    final int vl = vals.length;
    final Expr[] indexExprs = new Expr[vl];

    // find index equivalents
    for(int v = 0; v != vl; v++) {
      final byte[] lit = ((Literal) vals[v]).str();
      indexExprs[v] = new IndexAccess(indexType, lit);
    }

    ctx.compInfo(OPTONEOF);
    return new Path(new Union(indexExprs), path.invertPath(curr));
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr,
      final int min) {

    indexType = path.indexable(ctx, vals[0], cmp);
    if(indexType == null) return Integer.MAX_VALUE;

    int sum = 0;
    for(final Item val : vals) {
      final int nrIDs = ctx.local.data.nrIDs(indexType, val.str());
      sum += nrIDs;
      if(sum > min) return min;
    }
    return sum > min ? min : sum;
  }

  /** Index type (0 = none, 1 = text, 2 = word, 3 = attr). */
  private Index.TYPE indexType;

  @Override
  public String toString() {
    final TokenBuilder sb = new TokenBuilder();
    sb.add(name());
    sb.add("(" + path);
    for(int v = 0; v < vals.length; v++) {
      sb.add((v != 0 ? ", " : " " + cmp + " ") + vals[v]);
    }
    sb.add(")");
    return sb.toString();
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, TYPE, Token.token(cmp.toString()));
    path.plan(ser);
    for(final Expr val : vals) val.plan(ser);
    ser.closeElement(this);
  }
}
