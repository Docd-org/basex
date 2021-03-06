package org.basex.data.atomic;

import org.basex.data.*;

/**
 * Atomic update operation that inserts an attribute into a database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
final class InsertAttr extends BasicUpdate {
  /** Parent PRE of inserted nodes. */
  final int parent;
  /** Insertion sequence. */
  final DataClip insseq;

  /**
   * Constructor.
   * @param l PRE value of the target node location
   * @param par parent PRE value for the inserted node
   * @param c insert sequence data clip
   */
  InsertAttr(final int l, final int par, final DataClip c) {
    super(l, c.size(), l);
    parent = par;
    insseq = c;
  }

  @Override
  void apply(final Data d) {
    d.insertAttr(location, parent, insseq);
  }

  @Override
  DataClip getInsertionData() {
    return insseq;
  }

  @Override
  int parent() {
    return parent;
  }

  @Override
  boolean destructive() {
    return false;
  }

  @Override
  public String toString() {
    return "InsertAttr: " + location;
  }
}
