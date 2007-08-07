/****************************************************************************
Copyright (c) 2007, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package edu.mines.jtk.awt;

import java.awt.Color;
import java.awt.image.IndexColorModel;

import javax.swing.event.EventListenerList;

import edu.mines.jtk.util.Check;

/**
 * Transforms floats to colors for an index color model.
 * Two steps
 * <ol><li>
 * Map float to byte using clips/percentiles
 * </li><li>
 * Map byte to color using index color model.
 * </li></ol>
 * @author Dave Hale, Colorado School of Mines
 * @version 2007.08.07
 */
public class FloatIndexColorMap extends ColorMap {

  /**
   * Constructs a color map for specified values and index color model.
   * The integers 0 and 255 must be valid pixels for the color model.
   * @param fbm float byte map.
   * @param icm the index color model.
   */
  public FloatIndexColorMap(FloatByteMap fbm, IndexColorModel icm) {
    checkIndexColorModel(icm);
    _fbm = map;
    _icm = colorModel;
  }

  /**
   * Gets the color index corresponding to the specified value.
   * @param f the value to be mapped to index.
   * @return the index in the range [0,255].
   */
  public int getIndex(float f) {
    return _fbm.map(f);
  }

  /**
   * Gets the color for the specified value.
   * @param f the value to be mapped to a color.
   * @return the pixel.
   */
  public Color getColor(float f) {
    return _colors(getIndex(f));
  }

  /**
   * Gets the color (in standard ARGB format) for the specified value.
   * @param f the value to be mapped to a color.
   * @return the pixel.
   */
  public int getARGB(float f) {
    return _icm.getRGB(getIndex(f));
  }

  /**
   * Gets the minimum value in the range of mapped values.
   * @return the minimum value.
   */
  public double getMinValue() {
    return _fbm.getClipMin();
  }

  /**
   * Gets the maximum value in the range of mapped values.
   * @return the maximum value.
   */
  public double getMaxValue() {
    return _fbm.getClipMax();
  }

  /**
   * Gets the index color model used by this color map.
   * @return the index color model.
   */
  public IndexColorModel getColorModel() {
    return _icm;
  }

  /**
   * Gets the color corresponding to the specified value.
   * @param v the value to be mapped to a color.
   * @return the color.
   */
  public Color getColor(double v) {
    return getColor((float)v);
  }

  /**
   * Gets the index in the range [0,255] corresponding to the specified value.
   * @param v the value to be mapped to an index.
   * @return the index in the range [0,255].
   */
  public int getIndex(double v) {
    return getIndex((float)v);
  }

  /**
   * Sets the min-max range of values mapped to colors. Values outside this 
   * range are clipped. The default range is the min and max clips in the 
   * mapping from floats to bytes.
   * @param vmin the minimum value.
   * @param vmax the maximum value.
   */
  public void setValueRange(double vmin, double vmax) {
    if (vmin!=_fbm.
    _fbm.setClips(vmin,vmax);
    if (_vmin!=vmin || _vmax!=vmax) {
      _vmin = vmin;
      _vmax = vmax;
      fireColorMapChanged();
    }
  }

  /**
   * Sets the index color model for this color map.
   * @param colorModel the index color model.
   */
  public void setColorModel(IndexColorModel colorModel) {
    _colorModel = colorModel;
    cacheColors();
    fireColorMapChanged();
  }

  /**
   * Adds the specified color map listener.
   * Then notifies the listener that this colormap has changed.
   * @param cml the listener.
   */
  public void addListener(ColorMapListener cml) {
    _colorMapListeners.add(ColorMapListener.class,cml);
    cml.colorMapChanged(this);
  }

  /**
   * Removes the specified color map listener.
   * @param cml the listener.
   */
  public void removeListener(ColorMapListener cml) {
    _colorMapListeners.remove(ColorMapListener.class,cml);
  }

  /**
   * Gets a linear gray black-to-white color model.
   * @return the color model.
   */
  public static IndexColorModel getGray() {
    return getGray(0.0,1.0);
  }

  /**
   * Gets a linear gray color model for the specified gray levels. Gray
   * levels equal to 0.0 and 1.0 correspond to colors black and white, 
   * respectively.
   * @param g0 the gray level corresponding to index value 0.
   * @param g255 the gray level corresponding to index value 255.
   * @return the color model.
   */
  public static IndexColorModel getGray(double g0, double g255) {
    Color[] c = new Color[256];
    for (int i=0; i<256; ++i) {
      float g = (float)(g0+i*(g255-g0)/255.0);
      c[i] = new Color(g,g,g);
    }
    return makeIndexColorModel(c);
  }

  /**
   * Gets a red-to-blue color model like Matlab's jet color map.
   * @return the color model.
   */
  public static IndexColorModel getJet() {
    Color[] c = new Color[256];
    for (int i=0; i<256; ++i) {
      float x = (float)i/255.0f;
      if (x<0.125f) {
        float a = x/0.125f;
        c[i] = new Color(0.0f,0.0f,0.5f+0.5f*a);
      } else if (x<0.375f) {
        float a = (x-0.125f)/0.25f;
        c[i] = new Color(0.0f,a,1.0f);
      } else if (x<0.625f) {
        float a = (x-0.375f)/0.25f;
        c[i] = new Color(a,1.0f,1.0f-a);
      } else if (x<0.875f) {
        float a = (x-0.625f)/0.25f;
        c[i] = new Color(1.0f,1.0f-a,0.0f);
      } else {
        float a = (x-0.875f)/0.125f;
        c[i] = new Color(1.0f-0.5f*a,0.0f,0.0f);
      }
    }
    return makeIndexColorModel(c);
  }

  /**
   * Gets a color model with eight complete cycles of hues.
   * @return the color model.
   */
  public static IndexColorModel getPrism() {
    return getHue(0.0,8.0);
  }

  /**
   * Gets a red-to-blue linear hue color model.
   * @return the color model.
   */
  public static IndexColorModel getHue() {
    return getHue(0.0,0.67);
  }

  /**
   * Gets a linear hue color model for the specified hues. Hues equal to 
   * 0.00, 0.33, and 0.67, and 1.00 correspond approximately to the colors 
   * red, green, blue, and red, respectively.
   * @param h0 the hue corresponding to index value 0.
   * @param h255 the hue corresponding to index value 255.
   * @return the color model.
   */
  public static IndexColorModel getHue(double h0, double h255) {
    Color[] c = new Color[256];
    for (int i=0; i<256; ++i) {
      float h = (float)(h0+i*(h255-h0)/255.0);
      c[i] = Color.getHSBColor(h,1.0f,1.0f);
    }
    return makeIndexColorModel(c);
  }

  /**
   * Gets a red-white-blue color model.
   * @return the color model.
   */
  public static IndexColorModel getRedWhiteBlue() {
    Color[] c = new Color[256];
    for (int i=0; i<256; ++i) {
      float x = (float)i/255.0f;
      if (x<0.5f) {
        float a = x/0.5f;
        c[i] = new Color(1.0f,a,a);
      } else {
        float a = (x-0.5f)/0.5f;
        c[i] = new Color(1.0f-a,1.0f-a,1.0f);
      }
    }
    return makeIndexColorModel(c);
  }

  /**
   * Returns an index color model for the specified array of 256 colors.
   * @param c array[256] of colors.
   * @return the index color model.
   */
  public static IndexColorModel makeIndexColorModel(Color[] c) {
    return new IndexColorModel(8,256,getReds(c),getGreens(c),getBlues(c));
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private double _vmin = 0.0;
  private double _vmax = 1.0;
  private IndexColorModel _colorModel;
  private Color[] _colors = new Color[256];
  private EventListenerList _colorMapListeners = new EventListenerList();

  private void fireColorMapChanged() {
    Object[] listeners = _colorMapListeners.getListenerList();
    for (int i=listeners.length-2; i>=0; i-=2) {
      ColorMapListener cml = (ColorMapListener)listeners[i+1];
      cml.colorMapChanged(this);
    }
  }

  private void cacheColors() {
    for (int index=0; index<256; ++index)
      _colors[index] = new Color(_colorModel.getRGB(index));
  }

  private static byte[] getReds(Color[] color) {
    int n = color.length;
    byte[] r = new byte[n];
    for (int i=0; i<n; ++i)
      r[i] = (byte)color[i].getRed();
    return r;
  }

  private static byte[] getGreens(Color[] color) {
    int n = color.length;
    byte[] g = new byte[n];
    for (int i=0; i<n; ++i)
      g[i] = (byte)color[i].getGreen();
    return g;
  }

  private static byte[] getBlues(Color[] color) {
    int n = color.length;
    byte[] b = new byte[n];
    for (int i=0; i<n; ++i)
      b[i] = (byte)color[i].getBlue();
    return b;
  }

  private static byte[] getBytes(float[] f) {
    int n = f.length;
    byte[] b = new byte[n];
    for (int i=0; i<n; ++i)
      b[i] = (byte)(f[i]*255.0f+0.5f);
    return b;
  }

  private static void checkIndexColorModel(colorModel) {
    Check.argument(colorModel.isValid(0),"0 is valid for color model");
    Check.argument(colorModel.isValid(255),"255 is valid for color model");
  }
}