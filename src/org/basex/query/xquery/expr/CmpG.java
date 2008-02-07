package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.query.xquery.XQText.*;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;

/**
 * General comparison.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CmpG extends Arr {
  /** Comparators. */
  public enum COMP {
    /** General Comparison: less or equal. */
    LE("<=", CmpV.COMP.LE),
    /** General Comparison: less. */
    LT("<", CmpV.COMP.LT),
    /** General Comparison: greater of equal. */
    GE(">=", CmpV.COMP.GE),
    /** General Comparison: greater. */
    GT(">", CmpV.COMP.GT),
    /** General Comparison: equal. */
    EQ("=", CmpV.COMP.EQ),
    /** General Comparison: not equal. */
    NE("!=", CmpV.COMP.NE);

    /** String representation. */
    public final byte[] name;
    /** Comparator. */
    public final CmpV.COMP cmp;

    /**
     * Constructor.
     * @param n string representation
     * @param c comparator
     */
    COMP(final String n, final CmpV.COMP c) {
      name = Token.token(n);
      cmp = c;
    }
    
    @Override
    public String toString() { return Token.string(name); }
  };

  /** Comparator. */
  private final COMP cmp;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression
   * @param c comparator
   */
  public CmpG(final Expr e1, final Expr e2, final COMP c) {
    super(e1, e2);
    cmp = c;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);

    if(expr[0].e() || expr[1].e()) {
      ctx.compInfo(OPTPREEVAL, this);
      return Bln.FALSE;
    }
    if(expr[0].i() && expr[1].i()) {
      ctx.compInfo(OPTPREEVAL, this);
      return Bln.get(ev(iter(expr[0]), iter(expr[1])));
    }
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) {
    return new Iter() {
      /** Iterator flag. */
      private boolean more;
      @Override
      public Item next() throws XQException {
        return (more ^= true) ?
            Bln.get(ev(ctx.iter(expr[0]), ctx.iter(expr[1]))) : null;
      }
      @Override
      public String toString() {
        return CmpG.this.toString();
      }
    };
  }

  /**
   * Performs a general comparison on the specified items and comparator.
   * @param ir1 first item
   * @param ir2 second iterator
   * @return result of check
   * @throws XQException evaluation exception
   */
  protected boolean ev(final Iter ir1, final Iter ir2) throws XQException {
    Item it1, it2;

    final SeqIter seq = new SeqIter();
    if((it1 = ir1.next()) != null) {
      while((it2 = ir2.next()) != null) {
        if(ev(it1, it2, cmp.cmp)) return true;
        seq.add(it2);
      }
    }
    while((it1 = ir1.next()) != null) {
      final Iter ir3 = seq;
      ir3.reset();
      while((it2 = ir3.next()) != null) if(ev(it1, it2, cmp.cmp)) return true;
    }
    return false;
  }

  /**
   * Compares a single item.
   * @param c comparator
   * @param a first item to be compared
   * @param b second item to be compared
   * @return result of check
   * @throws XQException thrown if the items can't be compared
   */
  private static boolean ev(final Item a, final Item b, final CmpV.COMP c)
      throws XQException {

    if(a.type != b.type && !a.u() && !b.u() && (a.n() && !b.n() ||
        b.n() && !a.n() || !a.n() && !b.n()) && !(a.s() && b.s()))
      Err.cmp(a, b);
    return c.e(a, b);
  }
  
  @Override
  public Type returned() {
    return Type.BLN;
  }

  @Override
  public String info() {
    return "'" + cmp + "' expression";
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, TYPE, cmp.name, EVAL, ITER);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "FF9999";
  }

  @Override
  public String toString() {
    return toString(" " + cmp + " ");
  }
}
