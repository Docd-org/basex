package org.basex.gui.layout;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.dialog.Dialog;

/**
 * Project specific Slider implementation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXSlider extends BaseXPanel {
  /** Slider width. */
  private static final double SLIDERW = 20;
  /** Listener. */
  private Dialog listener;
  /** Minimum slider value. */
  private int min;
  /** Maximum slider value. */
  private int max;
  /** Current slider value. */
  private int curr;
  /** Current slider value. */
  private int oldCurr;
  /** Mouse position for dragging operations. */
  private int mouseX;
  
  /**
   * Constructor.
   * @param mn min value
   * @param mx max value
   * @param initial initial value
   * @param hlp help text
   * @param list listener
   */
  public BaseXSlider(final int mn, final int mx,
      final int initial, final byte[] hlp, final Dialog list) {

    super(hlp);
    listener = list;
    min = mn;
    max = mx;
    curr = initial;
    setFocusable(true);
    setMode(FILL.NONE);

    BaseXLayout.addDefaultKeys(this, listener);
    BaseXLayout.setHeight(this, getFont().getSize() + 3);

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        repaint();
      }
      @Override
      public void focusLost(final FocusEvent e) {
        repaint();
      }
    });
    
    addKeyListener(this);
    addMouseListener(this);
    addMouseMotionListener(this);
  }
  
  /**
   * Returns the current value.
   * @return current value
   */
  public int value() {
    return curr;
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    mouseX = e.getX();
    final double x = (curr - min) * (getWidth() - SLIDERW) / (max - min);
    setCursor(mouseX >= x && mouseX < x + SLIDERW ?
       GUIConstants.CURSORHAND : GUIConstants.CURSORARROW);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    requestFocusInWindow();
    mouseX = e.getX();
    final double w = getWidth() - SLIDERW;
    final double r = max - min;
    final double x = (curr - min) * w / r;
    if(mouseX < x || mouseX >= x + SLIDERW) curr = (int) (mouseX * r / w + min);
    oldCurr = curr;
    repaint();
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    final double prop = (max - min) * (mouseX - e.getX()) /
      (getWidth() - SLIDERW);
    curr = Math.max(min, Math.min(max, (int) (oldCurr - prop)));
    listener.action(null);
    repaint();
  }
  
  @Override
  public void keyPressed(final KeyEvent e) {
    final int code = e.getKeyCode();
    final int old = curr;
    if(code == KeyEvent.VK_LEFT || code == KeyEvent.VK_UP) {
      curr = Math.max(min, curr - 1);
    } else if(code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_DOWN) {
      curr = Math.min(max, curr + 1);
    } else if(code == KeyEvent.VK_PAGE_DOWN) {
      curr = Math.max(min, curr + 10);
    } else if(code == KeyEvent.VK_PAGE_UP) {
      curr = Math.min(max, curr - 10);
    } else if(code == KeyEvent.VK_HOME) {
      curr = min;
    } else if(code == KeyEvent.VK_END) {
      curr = max;
    }
    if(curr != old) {
      listener.action(null);
      repaint();
    }
  }
  
  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    
    final int w = getWidth();
    final int h = getHeight();
    final int hh = h / 2;
    
    g.setColor(hasFocus() ? Color.white : GUIConstants.color2);
    g.fillRect(0, hh - 2, w, 4);
    g.setColor(Color.black);
    g.drawLine(0, hh - 2, w, hh - 2);
    g.drawLine(0, hh - 2, 0, hh + 2);
    g.setColor(GUIConstants.color3);
    g.drawLine(w - 1, hh - 2, w - 1, hh + 2);
    g.drawLine(0, hh + 2, w, hh + 2);
    
    final double x = (curr - min) * (w - SLIDERW) / (max - min);
    BaseXLayout.drawCell(g, (int) x, (int) (x + SLIDERW), 2, h - 2, true);
  }
}
