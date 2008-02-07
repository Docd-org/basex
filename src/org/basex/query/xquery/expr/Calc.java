package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.item.Type.*;
import java.math.BigDecimal;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.DTd;
import org.basex.query.xquery.item.Dat;
import org.basex.query.xquery.item.Date;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Dec;
import org.basex.query.xquery.item.Dtm;
import org.basex.query.xquery.item.Dur;
import org.basex.query.xquery.item.Flt;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.Tim;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.item.YMd;
import org.basex.query.xquery.util.Err;

/**
 * Calculation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public enum Calc {
  /** Addition. */
  PLUS("+") {
    @Override
    public Item ev(final Item a, final Item b) throws XQException {
      final boolean t1 = a.u() || a.n();
      final boolean t2 = b.u() || b.n();
      if(t1 ^ t2) errNum(!t1 ? a : b);
      if(t1 && t2) {
        final Type t = type(a, b);
        if(t == Type.ITR) {
          long l1 = a.itr();
          long l2 = b.itr();
          checkRange(l1 + (double) l2);
          return Itr.get(l1 + l2);
        }
        if(t == Type.FLT) return Flt.get(a.flt() + b.flt());
        if(t == Type.DBL) return Dbl.get(a.dbl() + b.dbl());
        return Dec.get(a.dec().add(b.dec()));
      }

      if(a.type == b.type) {
        if(!a.d()) errNum(!t1 ? a : b);
        if(a.type == Type.YMD) return new YMd((YMd) a, (YMd) b, true);
        if(a.type == Type.DTD) return new DTd((DTd) a, (DTd) b, true);
      }
      if(a.type == Type.DTM) return new Dtm((Date) a, checkDur(b), true);
      if(b.type == Type.DTM) return new Dtm((Date) b, checkDur(a), true);
      if(a.type == Type.DAT) return new Dat((Date) a, checkDur(b), true);
      if(b.type == Type.DAT) return new Dat((Date) b, checkDur(a), true);
      if(a.type == Type.TIM) {
        if(b.type != Type.DTD) errType(Type.DTD, b);
        return new Tim((Tim) a, (DTd) b, true);
      }
      if(b.type == Type.TIM) {
        if(a.type != Type.DTD) errType(Type.DTD, b);
        return new Tim((Tim) b, (DTd) a, true);
      }
      errType(a.type, b);
      return null;
    }
  },

  /** Subtraction. */
  MINUS("-") {
    @Override
    public Item ev(final Item a, final Item b) throws XQException {
      final boolean t1 = a.u() || a.n();
      final boolean t2 = b.u() || b.n();
      if(t1 ^ t2) errNum(!t1 ? a : b);
      if(t1 && t2) {
        final Type t = type(a, b);
        if(t == Type.ITR) {
          long l1 = a.itr();
          long l2 = b.itr();
          checkRange(l1 - (double) l2);
          return Itr.get(l1 - l2);
        }
        if(t == Type.FLT) return Flt.get(a.flt() - b.flt());
        if(t == Type.DBL) return Dbl.get(a.dbl() - b.dbl());
        return Dec.get(a.dec().subtract(b.dec()));
      }

      if(a.type == b.type) {
        if(a.type == Type.DTM || a.type == Type.DAT || a.type == Type.TIM)
          return new DTd((Date) a, (Date) b);
        if(a.type == Type.YMD) return new YMd((YMd) a, (YMd) b, false);
        if(a.type == Type.DTD) return new DTd((DTd) a, (DTd) b, false);
        errNum(!t1 ? a : b);
      }
      if(a.type == Type.DTM)
        return new Dtm((Date) a, checkDur(b), false);
      if(a.type == Type.DAT)
        return new Dat((Date) a, checkDur(b), false);
      if(a.type == Type.TIM) {
        if(b.type != Type.DTD) errType(Type.DTD, b);
        return new Tim((Tim) a, (DTd) b, false);
      }
      errType(a.type, b);
      return null;
    }
  },

  /** Multiplication. */
  MULT("*") {
    @Override
    public Item ev(final Item a, final Item b) throws XQException {
      if(a.type == Type.YMD) {
        if(!b.n()) errNum(b);
        return new YMd((Dur) a, b.dbl(), true);
      }
      if(b.type == Type.YMD) {
        if(!a.n()) errNum(a);
        return new YMd((Dur) b, a.dbl(), true);
      }
      if(a.type == Type.DTD) {
        if(!b.n()) errNum(b);
        return new DTd((Dur) a, b.dbl(), true);
      }
      if(b.type == Type.DTD) {
        if(!a.n()) errNum(a);
        return new DTd((Dur) b, a.dbl(), true);
      }

      final boolean t1 = a.u() || a.n();
      final boolean t2 = b.u() || b.n();
      if(t1 ^ t2) errType(a.type, b);
      if(t1 && t2) {
        final Type t = type(a, b);
        if(t == Type.ITR) {
          long l1 = a.itr();
          long l2 = b.itr();
          checkRange(l1 * (double) l2);
          return Itr.get(l1 * l2);
        }
        if(t == Type.FLT) return Flt.get(a.flt() * b.flt());
        if(t == Type.DBL) return Dbl.get(a.dbl() * b.dbl());
        return Dec.get(a.dec().multiply(b.dec()));
      }
      errNum(!t1 ? a : b);
      return null;
    }
  },

  /** Division. */
  DIV("div") {
    @Override
    public Item ev(final Item a, final Item b) throws XQException {
      if(a.type == b.type) {
        if(a.type == Type.YMD) {
          final YMd d1 = (YMd) a;
          final YMd d2 = (YMd) b;
          final double v1 = d1.minus ? -d1.mon : d1.mon;
          final double v2 = d2.minus ? -d2.mon : d2.mon;
          if(v2 == 0) Err.or(DIVZERO, d1);
          return Dec.get(BigDecimal.valueOf(v1).divide(
              BigDecimal.valueOf(v2), 20, BigDecimal.ROUND_HALF_EVEN));
        }
        if(a.type == Type.DTD) {
          final DTd d1 = (DTd) a;
          final DTd d2 = (DTd) b;
          final double v1 = d1.minus ? -d1.sec - d1.mil : d1.sec + d1.mil;
          final double v2 = d2.minus ? -d2.sec - d2.mil : d2.sec + d2.mil;
          if(v2 == 0) Err.or(DIVZERO, d1);
          return Dec.get(BigDecimal.valueOf(v1).divide(
              BigDecimal.valueOf(v2), 20, BigDecimal.ROUND_HALF_EVEN));
        }
      }

      if(a.type == Type.YMD) {
        if(!b.n()) errNum(b);
        return new YMd((Dur) a, b.dbl(), false);
      }
      if(a.type == Type.DTD) {
        if(!b.n()) errNum(b);
        return new DTd((Dur) a, b.dbl(), false);
      }

      checkNum(a, b);
      final Type t = type(a, b);
      if(t == Type.DBL) return Dbl.get(a.dbl() / b.dbl());
      if(t == Type.FLT) return Flt.get(a.flt() / b.flt());

      final BigDecimal b1 = a.dec();
      final BigDecimal b2 = b.dec();
      if(b2.signum() == 0) Err.or(DIVZERO, a);
      final int s = Math.max(18, Math.max(b1.scale(), b2.scale()));
      return Dec.get(b1.divide(b2, s, BigDecimal.ROUND_HALF_EVEN));
    }
  },

  /** Integer division. */
  IDIV("idiv") {
    @Override
    public Item ev(final Item a, final Item b) throws XQException {
      checkNum(a, b);
      final double d1 = a.dbl();
      final double d2 = b.dbl();
      if(d2 == 0) Err.or(DIVZERO, a);
      final double d = d1 / d2;
      if(d != d || d == 1 / 0.0 || d == -1 / 0.0) Err.or(DIVFLOW, d1, d2);

      final Type t = type(a, b);
      return Itr.get(t == Type.ITR ? a.itr() / b.itr() : (long) d);
    }
  },

  /** Modulo. */
  MOD("mod") {
    @Override
    public Item ev(final Item a, final Item b) throws XQException {
      checkNum(a, b);
      final Type t = type(a, b);
      if(t == Type.DBL) return Dbl.get(a.dbl() % b.dbl());
      if(t == Type.ITR) return Itr.get(a.itr() % b.itr());
      if(t == Type.FLT) return Flt.get(a.flt() % b.flt());

      final BigDecimal b1 = a.dec();
      final BigDecimal b2 = b.dec();
      final BigDecimal q = b1.divide(b2, 0, BigDecimal.ROUND_DOWN);
      return Dec.get(b1.subtract(q.multiply(b2)));       
    }
  };

  /** Name of operation. */
  public final String name;

  /**
   * Constructor.
   * @param n name
   */
  Calc(final String n) {
    name = n;
  }

  /**
   * Performs the calculation.
   * @param a first item
   * @param b second item
   * @return result type
   * @throws XQException evaluation exception
   */
  public abstract Item ev(final Item a, final Item b) throws XQException;

  /**
   * Returns the numeric type with the highest precedence.
   * @param a first item
   * @param b second item
   * @return type
   */
  public static final Type type(final Item a, final Item b) {
    if(a.type == DBL || b.type == DBL || a.u() || b.u()) return DBL;
    if(a.type == FLT || b.type == FLT) return FLT;
    if(a.type == DEC || b.type == DEC) return DEC;
    return ITR;
  }

  /**
   * Returns a type error.
   * @param t expected type
   * @param it item
   * @throws XQException evaluation exception
   */
  protected final void errType(final Type t, final Item it) throws XQException {
    Err.type(info(), t, it);
  }

  /**
   * Returns a numeric type error.
   * @param it item
   * @throws XQException evaluation exception
   */
  protected final void errNum(final Item it) throws XQException {
    Err.num(info(), it);
  }

  /**
   * Returns a duration type error.
   * @param it item
   * @return duration
   * @throws XQException evaluation exception
   */
  protected final Dur checkDur(final Item it) throws XQException {
    if(!it.d()) Err.or(XPDUR, info(), it.type);
    return (Dur) it;
  }

  /**
   * Checks if the specified items are numeric or untyped. 
   * @param a first item
   * @param b second item
   * @throws XQException evaluation exception
   */
  protected final void checkNum(final Item a, final Item b) throws XQException {
    if(!a.u() && !a.n()) errNum(a);
    if(!b.u() && !b.n()) errNum(b);
  }
  
  /**
   * Checks if the specified value is outside the integer range. 
   * @param d value to be checked
   * @throws XQException evaluation exception
   */
  protected void checkRange(final double d) throws XQException {
    if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) Err.or(RANGE, d);
  }

  @Override
  public String toString() { return name; }

  /**
   * Returns a string representation of the operator.
   * @return string
   */
  public String info() {
    return "'" + name + "' operator";
  }
}
