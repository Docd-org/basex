package org.basex.query.xpath.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;

/**
 * Logical FTUnaryNot expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTUnaryNot extends FTArrayExpr {
  /**
   * Constructor.
   * @param e expressions
   */
  public FTUnaryNot(final FTArrayExpr[] e) {
    exprs = e;
  }

  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    return Bool.get(!exprs[0].eval(ctx).bool()); 
  }

  @Override
  public FTArrayExpr compile(final XPContext ctx) throws QueryException {
    for(int i = 0; i != exprs.length; i++) {
      if(exprs[i].fto == null) exprs[i].fto = fto;
      exprs[i] = exprs[i].compile(ctx);
    }
    return this;
  }
  
  @Override
  public FTArrayExpr indexEquivalent(final XPContext ctx, final Step curr, 
      final boolean seq)
      throws QueryException {
   
    final FTArrayExpr[] indexExprs = new FTArrayExpr[exprs.length];
    
    // find index equivalents
    for(int i = 0; i != exprs.length; i++) {
      indexExprs[i] = exprs[i].indexEquivalent(ctx, curr, seq);
      if(indexExprs[i] == null) indexExprs[i] = exprs[i];
    }
/*
    // perform path step only once if all path expressions are the same
    final Expr[] ex = XPOptimizer.getIndexExpr(indexExprs);
    if(ex != null) return new Path(new FTNotExprs(ex),
        ((Path) indexExprs[0]).path);*/
    return new FTUnaryNotExprs(indexExprs);
  }
  
  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    if (exprs.length == 1) {
      exprs[0].indexSizes(ctx, curr, min);
      return Integer.MAX_VALUE;
      //if (nrIDs == 0) return Integer.MAX_VALUE;
      //return nrIDs;
    }
    
    // should not happen
    return Integer.MAX_VALUE;
    //return Integer.MAX_VALUE;
/*    int sum = 0;
    for(final Expr expr : exprs) {
      final int nrIDs = expr.indexSizes(ctx, curr, min);
      if(nrIDs == Integer.MAX_VALUE) return nrIDs;
      sum += nrIDs;
      if(sum > min) return min;
    }
    return sum > min ? min : sum;
*/
  }
  
  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(Expr e : exprs) e.plan(ser);
    ser.closeElement();
  }
}
