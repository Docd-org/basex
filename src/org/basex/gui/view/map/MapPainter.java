package org.basex.gui.view.map;

import java.awt.Color;
import java.awt.Graphics;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;

/**
 * Provides an interface for data specific TreeMap visualizations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
abstract class MapPainter {
  /** Graphics reference. */
  MapView view;
  /** Marked position. */
  int mpos;
  
  /**
   * Constructor.
   * @param m map reference.
   */
  MapPainter(final MapView m) {
    view = m;
  }
  
  /**
   * Returns next color mark.
   * @param rects rectangle array
   * @param pre pre array
   * @param ri current position
   * @param rs array size
   * @return next color mark
   */
  Color nextMark(final MapRects rects, final int pre, final int ri,
      final int rs) {

    final Nodes marked = GUI.context.marked();
    // checks if the current node is a queried context node
    while(mpos < marked.size && marked.pre[mpos] < pre) mpos++;
    if(mpos < marked.size) {
      if(marked.pre[mpos] == pre) {
        // mark node
        return GUIConstants.colormark1;
      } else if(ri + 1 < rs && marked.pre[mpos] < rects.get(ri + 1).p) {
        // mark ancestor of invisible node
        return GUIConstants.colormark2;
      }
    }
    return null;
  }

  /**
   * Paints node contents.
   * @param g graphics reference
   * @param rects rectangle array
   */
  abstract void drawRectangles(final Graphics g, final MapRects rects);

  /**
   * Checks mouse activity.
   * @param click mouse click
   * @param rect current rectangle
   * @param mx mouse x
   * @param my mouse y
   * @return true for mouse activity
   */
  abstract boolean highlight(MapRect rect, int mx, int my, boolean click);

  /**
   * Initializes the skipping of nodes.
   * @param rects rectangle array
   */
  abstract void init(MapRects rects);

  /**
   * Resets painter.
   */
  abstract void reset();
}
