package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQTokens.*;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQTokens;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.iter.NodeMore;
import org.basex.query.xquery.iter.NodeNext;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * Disk-based Node item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DNode extends Node {
  /** Root node (constructor). */
  public Node root;
  /** Data reference. */
  public Data data;
  /** Pre value. */
  public int pre;

  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   * @param r parent reference
   * @param t node type
   */
  public DNode(final Data d, final int p, final Node r, final Type t) {
    super(t);
    data = d;
    par = r;
    pre = p;
  }

  @Override
  public byte[] str() {
    return data.atom(pre);
  }

  @Override
  public void serialize(final Serializer ser, final XQContext ctx,
      final int level) throws Exception {

    switch(type) {
      case ATT:
        ser.attribute(data.attName(pre), data.attValue(pre));
        break;
      case COM:
        ser.comment(str());
        break;
      case DOC:
        serElem(ser, ctx, 1, level);
        break;
      case ELM:
        serElem(ser, ctx, pre, level);
        break;
      case PI:
        ser.pi(str());
        break;
      case TXT:
        ser.text(str());
        break;
      default:
        throw new RuntimeException("Unknown node type: " + type);
    }
  }

  @Override
  public String toString() {
    switch(type) {
      case ATT:
        return type + "(" + Token.string(data.attName(pre)) + "=\"" +
        Token.string(data.attValue(pre)) + "\")";
      case DOC:
        return type + "(" + data.meta.filename + ")";
      case ELM:
        return type + "(" + Token.string(data.tag(pre)) + "/" + pre + ")";
      default:
        return type + "(" + Token.string(str()) + ")";
    }
  }

  @Override
  public byte[] nname() {
    switch(type) {
      case ATT:
        return data.attName(pre);
      case ELM:
        return data.tag(pre);
      case PI:
        byte[] name = data.text(pre);
        final int i = Token.indexOf(name, ' ');
        if(i != -1) name = Token.substring(name, 0, i);
        return name;
      default:
        return Token.EMPTY;
    }
  }

  @Override
  public QNm qname() {
    return new QNm(nname());
  }
  
  @Override
  public QNm qname(final QNm nm) {
    nm.name(nname());
    // <CG> DNode/Namespaces: introduce correct namespace handling
    if(data.xmlnsID != 0) {
      int s = pre + data.attSize(pre, data.kind(pre));
      int p = pre;
      while(++p != s) {
        if(data.attNameID(p) == data.xmlnsID) {
          nm.uri = Uri.uri(data.attValue(p));
          break;
        }
      }
    }
    return nm;
  }

  @Override
  public byte[] base() {
    return type != Type.DOC ? Token.EMPTY : Token.token(data.meta.filename);
  }

  @Override
  public boolean is(final Node nod) {
    if(nod == this) return true;
    if(!(nod instanceof DNode)) return false;
    return data == ((DNode) nod).data && pre == ((DNode) nod).pre;
  }

  @Override
  public int diff(final Node nod) {
    if(!(nod instanceof DNode) || data != ((DNode) nod).data)
      return id - nod.id;
    return pre - ((DNode) nod).pre;
  }

  @Override
  public DNode copy() {
    // par.finish() ?..
    final DNode node = new DNode(data, pre, par, type);
    node.root = root;
    node.score(score());
    return node;
  }

  @Override
  public DNode finish() {
    return copy();
  }

  @Override
  public Node parent() {
    if(par != null) return par;
    final int p = data.parent(pre, data.kind(pre));
    // check if parent constructor exists; if not, include document root node
    if(p == (root != null ? 0 : -1)) return root;
    final DNode node = copy();
    node.set(p, data.kind(p));
    return node;
  }

  @Override
  public void parent(final Node p) {
    root = p;
    par = p;
  }

  /**
   * Serializes the specified node, starting from the specified pre value.
   * @param ser result reader
   * @param ctx xquery context
   * @param pos pre value
   * @param level current level
   * @throws Exception exception
   */
  public void serElem(final Serializer ser, final XQContext ctx,
      final int pos, final int level) throws Exception {

    // stacks
    final int[] parent = new int[256];
    final byte[][] token = new byte[256][];
    // current output level
    int l = 0;
    int p = pos;

    // start with the root node
    final int r = p;

    // loop through all table entries
    final int s = data.size;
    while(p < s) {
      final int kind = data.kind(p);
      final int pr = data.parent(p, kind);
      // skip writing if all sub nodes were processed
      if(r != 1 && p > r && pr < r) break;

      // close opened tags...
      while(l > 0) {
        if(parent[l - 1] < pr) break;
        ser.closeElement(token[--l]);
      }

      if(kind == Data.TEXT) {
        ser.text(data.text(p++));
      } else if(kind == Data.COMM) {
        ser.comment(data.text(p++));
      } else if(kind == Data.PI) {
        ser.pi(data.text(p++));
      } else {
        // add element node
        final byte[] name = data.tag(p);
        ser.startElement(name);

        // find attributes
        final int ps = p + data.size(p, kind);
        final int as = p + data.attSize(p, kind);

        if(level != 0 || l != 0) {
          while(++p != as) {
            ser.attribute(data.attName(p), data.attValue(p));
          }
        } else {
          final TokenList names = new TokenList();
          final TokenList values = new TokenList();
          
          int pp = p;
          while(++p != as) {
            byte[] at = data.attName(p);
            names.add(at);
            values.add(data.attValue(p));
          }
          
          final int i = Token.indexOf(name, ':');
          if(i != -1) {
            final byte[] pref = Token.substring(name, 0, i);
            final byte[] uri = ctx.ns.uri(pref).str();
            final byte[] at = Token.concat(XMLNSCOL, pref);
            if(!names.contains(at)) {
              names.add(at);
              values.add(uri);
            }
          }
          int p2 = pp;
          while(p2 != 0) {
            pp = p2;
            p2 = data.parent(p2, data.kind(p2));
          }
          final int pps = pp + data.attSize(pp, data.kind(pp));
          while(++pp != pps) {
            byte[] at = data.attName(pp);
            if(Token.startsWith(at, XQTokens.XMLNS) && !names.contains(at)) {
              names.add(at);
              values.add(data.attValue(pp));
            }
          }
          for(int n = 0; n < names.size; n++) {
            ser.attribute(names.list[n], values.list[n]);
          }
        }
  
        // check if this is an empty tag
        if(as == ps) {
          ser.emptyElement();
        } else {
          ser.finishElement();
          token[l] = name;
          parent[l++] = pr;
        }
      }
    }
    // process nodes that remain in the stack
    while(l > 0) ser.closeElement(token[--l]);
  }

  @Override
  public NodeIter anc() {
    return new NodeIter() {
      /** Temporary node. */
      private Node node = DNode.this;

      @Override
      public Node next() {
        node = node.parent();
        return node;
      }
    };
  }

  @Override
  public NodeIter ancOrSelf() {
    return new NodeIter() {
      /** Temporary node. */
      private Node node = DNode.this;

      @Override
      public Node next() {
        if(node == null) return null;
        final Node n = node;
        node = n.parent();
        return n;
      }
    };
  }

  @Override
  public NodeIter attr() {
    return new NodeIter() {
      /** Temporary node. */
      private final DNode node = copy();
      /** Current pre value. */
      private int p = pre + 1;
      /** Current size value. */
      private final int s = pre + data.attSize(pre, data.kind(pre));

      @Override
      public Node next() {
        if(p == s) return null;
        node.set(p++, Data.ATTR);
        return node;
      }
    };
  }

  @Override
  public NodeMore child() {
    return new NodeMore() {
      /** Temporary node. */
      private final DNode node = copy();
      /** First call. */
      private boolean more;
      /** Current pre value. */
      private int p;
      /** Current size value. */
      private int s;

      @Override
      public boolean more() {
        if(!more) {
          final int k = data.kind(pre);
          p = pre + data.attSize(pre, k);
          s = pre + data.size(pre, k);
          more = true;
        }
        return p != s;
      }

      @Override
      public Node next() {
        if(!more()) return null;
        final int k = data.kind(p);
        node.set(p, k);
        p += data.size(p, k);
        return node;
      }
    };
  }

  @Override
  public NodeIter desc() {
    return new NodeIter() {
      /** Temporary node. */
      private final DNode node = copy();
      /** Current pre value. */
      private int p = pre + data.attSize(pre, data.kind(pre));
      /** Current size value. */
      private final int s = pre + data.size(pre, data.kind(pre));

      @Override
      public Node next() {
        if(p == s) return null;
        final int k = data.kind(p);
        node.set(p, k);
        p += data.attSize(p, k);
        return node;
      }
    };
  }

  @Override
  public NodeIter descOrSelf() {
    return new NodeIter() {
      /** Temporary node. */
      private final DNode node = copy();
      /** Current pre value. */
      private int p = pre;
      /** Current size value. */
      private final int s = pre + data.size(pre, data.kind(pre));

      @Override
      public Node next() {
        if(p == s) return null;
        final int k = data.kind(p);
        node.set(p, k);
        p += data.attSize(p, k);
        return node;
      }
    };
  }

  @Override
  public NodeIter foll() {
    return new NodeIter() {
      /** Iterator. */
      private NodeNext it;
      /** First call. */
      private boolean more;

      @Override
      public Node next() throws XQException {
        if(!more) {
          final NodIter ch = new NodIter();
          final Node nod = DNode.this;
          Node p = nod.parent();
          while(p != null) {
            final NodeIter i = p.child();
            Node nn;
            while((nn = i.next()) != null) {
              if(nn.is(nod)) break;
            }
            while((nn = i.next()) != null) {
              ch.add(nn.finish());
              addDesc(nn.child(), ch);
            }
            p = p.parent();
          }
          it = new NodeNext(ch);
          more = true;
        }
        return it.next();
      }
    };
  }

  @Override
  public NodeIter follSibl() {
    return new NodeIter() {
      /** Iterator. */
      private NodeIter it;
      /** First call. */
      private boolean more;

      @Override
      public Node next() throws XQException {
        if(!more) {
          final Node r = parent();
          if(r == null) {
            it = NodeIter.NONE;
          } else {
            it = r.child();
            Node n;
            while((n = it.next()) != null && !n.is(DNode.this));
          }
          more = true;
        }
        return it.next();
      }
    };
  }

  @Override
  public NodeIter par() {
    return new NodeIter() {
      /** First call. */
      private boolean more;

      @Override
      public Node next() {
        more ^= true;
        return more ? parent() : null;
      }
    };
  }

  @Override
  public NodeIter prec() {
    return new NodeIter() {
      /** Iterator. */
      private NodeIter it;
      /** First call. */
      private boolean more;

      @Override
      public Node next() throws XQException {
        if(!more) {
          final NodIter ch = new NodIter();
          NodIter tmp = new NodIter();
          Node nod = DNode.this;
          Node r = nod.parent();
          while(r != null) {
            tmp = new NodIter();
            final NodeIter itr = r.child();
            Node n;
            while((n = itr.next()) != null) {
              if(!n.is(nod)) tmp.add(n.finish());
              else break;
            }
            int i = tmp.size;
            while(--i >= 0) {
              ch.add(tmp.list[i]);
              addDesc(tmp.list[i].child(), ch);
            }
            nod = r;
            r = r.parent();
          }
          it = new NodeNext(ch);
          more = true;
        }
        return it.next();
      }
    };
  }

  @Override
  public NodeIter precSibl() {
    return new NodeIter() {
      /** Children nodes. */
      private NodIter ch;
      /** Counter. */
      private int c;
      /** First call. */
      private boolean more;

      @Override
      public Node next() throws XQException {
        if(!more) {
          final Node r = parent();
          if(r == null) return null;

          ch = new NodIter();
          final NodeIter iter = r.child();
          Node n;
          while((n = iter.next()) != null) {
            if(!n.is(DNode.this)) ch.add(n.finish());
            else break;
          }
          c = ch.size;
          more = true;
        }
        return c == 0 ? null : ch.list[--c];
      }
    };
  }

  @Override
  public NodeIter self() {
    return new NodeIter() {
      /** First call. */
      private boolean more;

      @Override
      public Node next() {
        more ^= true;
        return more ? DNode.this : null;
      }
    };
  }

  /** Node Types. */
  private static final Type[] TYPES = {
    Type.DOC, Type.ELM, Type.TXT, Type.ATT, Type.COM, Type.PI
  };
  
  /**
   * Sets the node type.
   * @param p pre value
   * @param k node kind
   */
  public void set(final int p, final int k) {
    type = TYPES[k];
    par = null;
    pre = p;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this, PRE, Token.token(pre));
  }
}
