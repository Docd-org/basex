package org.basex.query.xpath.values;

import java.util.Arrays;
import org.basex.util.Array;

/**
 * NodeSet Constructor.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class NodeBuilder {
  /** Node array. */
  public int[] nodes;
  /** Size of node array. */
  public int size;
  /** Sorting flag. */
  private boolean sort;

  /**
   * Constructor, creating an empty node set.
   */
  public NodeBuilder() {
    nodes = new int[1];
  }

  /**
   * Constructor, creating a new node set from the specified node ids.
   * @param ids node ids
   */
  public NodeBuilder(final int[] ids) {
    if(ids.length == 0) {
      nodes = new int[1];
    } else {
      nodes = ids;
      size = ids.length;
    }
  }

  /**
   * Adds a pre value to the node set.
   * @param pre value to be added.
   */
  public void add(final int pre) {
    if(size == nodes.length) nodes = Array.extend(nodes);
    if(!sort && size != 0) {
      final int d = pre - nodes[size - 1];
      if(d == 0) return;
      sort = d <= 0;
    }
    nodes[size++] = pre;
  }

  /**
   * Adds a node set.
   * @param build node set to be added.
   */
  public void add(final NodeBuilder build) {
    final int[] set = build.nodes;
    final int sl = build.size;
    if(sl == 0) return;

    if(sl == 1) {
      add(set[0]);
    } else {
      final int s = size + sl;
      int t = nodes.length;
      while(t <= s) t <<= 1;
      if(t != nodes.length) nodes = Array.resize(nodes, size, t);
      System.arraycopy(set, 0, nodes, size, sl);
      size = s;
      sort = true;
    }
  }

  /**
   * Reset the NodeSet so it can be reused.
   */
  public void reset() {
    size = 0;
    sort = false;
  }

  /**
   * Returns the node array after sorting and duplicate elimination.
   * @return node array.
   */
  public int[] finish() {
    if(sort && size >= 2) {
      Arrays.sort(nodes, 0, size);
      int j = 0;
      for(int i = 1; i != size; i++) {
        if(nodes[j] != nodes[i]) nodes[++j] = nodes[i];
      }
      size = ++j;
      sort = false;
    }
    return Array.finish(nodes, size);
  }

  /**
   * Set node array with prevalues.
   *
   * @param pres to set
   */
  public void setNodes(final int[] pres) {
    size = pres.length;
    nodes = pres;
    //nodes = new int[size];
    //System.arraycopy(pres, 0, nodes, 0, size);
  }
}
