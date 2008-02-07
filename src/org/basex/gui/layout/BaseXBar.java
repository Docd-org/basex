package org.basex.gui.layout;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.FILL;
import org.basex.util.Performance;

/**
 * This is a scrollbar implementation, supporting arbitrary
 * panel heights without increasing the memory consumption.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXBar extends BaseXPanel {
  /** Scrollbar size. */
  public static final int SIZE = 16;
  /** Maximum scrolling speed.  */
  static final int MAXSTEP = 15;
  /** Animated scrollbar zooming steps. */
  static final int[] STEPS = { -MAXSTEP, -14, -11, -8, -6, -4, -3,
      -2, -1, -1, 0, 0, 1, 1, 2, 3, 4, 6, 8, 11, 14, MAXSTEP };
  /** Minimum size for the scrollbar slider. */
  private static final int MINSIZE = 10;

  /** Current scrolling speed. */
  int step = STEPS.length / 2;
  /** Flag reporting if the scrollbar animation is running. */
  boolean animated;
  /** Reference to the scrolled component. */
  JComponent comp;
  /** Scrollbar width. */
  int ww;
  /** Scrollbar height. */
  int hh;
  /** Scrollbar slider position. */
  int barPos;
  /** Scrollbar slider size. */
  int barSize;
  /** Scrollbar dragging position. */
  int dragPos;
  /** Flag for button clicks. */
  boolean button;
  /** Flag for scrolling upward. */
  boolean up;
  /** Flag for scrolling downward. */
  boolean down;
  /** Flag for sliding the scrollbar. */
  boolean sliding;
  /** Flag for moving upward. */
  boolean moving;
  /** Current panel position. */
  int pos;
  /** Current panel height. */
  int height;
  /** Scrollbar slider offset. */
  private int barOffset;
  /** Flag for permanent scrollbar visibility. */
  private boolean visible;

  /**
   * Default Constructor. By default, the scrollbar is switched off
   * if the component is completely displayed.
   * @param cmp reference to the scrolled component
   */
  public BaseXBar(final JComponent cmp) {
    this(cmp, false);
  }

  /**
   * Default Constructor, allowing to modify the scrollbar visibility.
   * @param cmp reference to the scrolled component
   * @param vis states if scrollbar is always visible or hidden when
   * the displayed content needs no scrollbar.
   */
  public BaseXBar(final JComponent cmp, final boolean vis) {
    super(null);
    comp = cmp;
    visible = vis;
    addMouseListener(this);
    addMouseMotionListener(this);
    setMode(FILL.NONE);
    BaseXLayout.setWidth(this, SIZE);
    ww = SIZE;
  }

  /**
   * Sets the vertical scrollbar slider position.
   * @param p vertical position
   */
  public void pos(final int p) {
    pos = Math.max(0, Math.min(height - getHeight(), p));
    repaint();
  }

  /**
   * Returns the vertical scrollbar slider position.
   * @return vertical position
   */
  public int pos() {
    return pos;
  }

  /**
   * Sets the panel height.
   * @param h panel height
   */
  public void height(final int h) {
    height = h;
    repaint();
  }

  /**
   * Returns the panel height.
   * @return panel height
   */
  public int height() {
    return height;
  }

  @Override
  public void paintComponent(final Graphics g) {
    hh = getHeight();
    super.paintComponent(g);
    
    if(!visible && hh >= height) return;

    // calculate bar size
    final int barH = hh - ww * 2 + 4;
    final float factor = (barH - barOffset) / (float) height;
    int size = (int) (hh * factor);
    // define minimum size for scrollbar mover
    barOffset = size < MINSIZE ? MINSIZE - size : 0;
    size += barOffset;
    barPos = (int) Math.max(0, Math.min(pos * factor, barH - barSize));
    barSize = Math.min(size, barH - 1);

    // paint scrollbar background
    g.setColor(GUIConstants.color5);
    g.drawLine(0, 0, 0, hh);

    // draw scroll up button
    int x = 0;
    int y = 0;

    BaseXLayout.drawCell(g, 0, ww, y, y + ww - 1, false);

    int xx = x + SIZE / 2 - 3;
    int yy = y + SIZE / 2 - 3;
    if(button && up) { xx++; yy++; }
    g.setColor(GUIConstants.color6);
    g.drawLine(xx + 2, yy    , xx + 2, yy);
    g.drawLine(xx + 1, yy + 1, xx + 3, yy + 1);
    g.drawLine(xx + 1, yy + 2, xx + 3, yy + 2);
    g.drawLine(xx    , yy + 3, xx + 4, yy + 3);
    g.drawLine(xx    , yy + 4, xx + 4, yy + 4);

    // draw scroll down button
    x = 0;
    y = Math.max(SIZE, hh - ww);

    BaseXLayout.drawCell(g, 0, ww, y, y + ww - 1, false);

    xx = x + SIZE / 2 - 3;
    yy = y + SIZE / 2 - 3;
    if(button && down) { xx++; yy++; }
    g.setColor(GUIConstants.color6);
    g.drawLine(xx + 2, yy + 4, xx + 2, yy + 4);
    g.drawLine(xx + 1, yy + 3, xx + 3, yy + 3);
    g.drawLine(xx + 1, yy + 2, xx + 3, yy + 2);
    g.drawLine(xx    , yy + 1, xx + 4, yy + 1);
    g.drawLine(xx    , yy    , xx + 4, yy);

    // draw scroll slider
    BaseXLayout.drawCell(g, 0, ww, ww - 2 + barPos, ww - 2 + barPos + barSize,
        false);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    final int y = e.getY();
    sliding = y > ww + barPos && y < ww + barPos + barSize;
    moving = !sliding;
    up = y < ww + barPos;
    down = y > ww + barPos + barSize;
    button = y < ww || y > hh - ww;
    if(sliding) dragPos = barPos - y;

    // start dragging
    if(sliding || animated) return;

    new Thread() {
      @Override
      public void run() {
        // scroll up/down/move slider
        animated = moving;
        while(animated) {
          if(moving)
            step = Math.max(0, Math.min(STEPS.length - 1,
                step + (down ? 1 : -1)));
          else
            step += step < STEPS.length / 2 ? 1 : -1;
          int offset = STEPS[step];

          if(!button) {
            offset = offset * hh / MAXSTEP / 4;
          }
          pos = Math.max(0, Math.min(height - hh, pos + offset));
          comp.repaint();
          Performance.sleep(25);
          animated = step != STEPS.length / 2;

          if(y > ww + barPos && y < ww + barPos + barSize) {
            dragPos = barPos - y;
            animated = false;
            sliding = true;
            step = STEPS.length / 2;
          }
        }
      }
    }.start();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    up = false;
    down = false;
    moving = false;
    sliding = false;
    comp.repaint();
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    // no dragging...
    if(!sliding) return;

    pos = (int) ((long) (e.getY() + dragPos) * height / (hh - ww * 2));
    pos = Math.max(0, Math.min(height - hh, pos));
    comp.repaint();
  }
}
