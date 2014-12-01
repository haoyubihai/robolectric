package org.robolectric.shadows;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.util.ReflectionHelpers;

import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.internal.Shadow.directlyOn;
import static org.robolectric.internal.Shadow.invokeConstructor;

/**
 * Shadow implementation of {@code View} that simulates the behavior of this
 * class.
 *
 * <p>
 * Supports listeners, focusability (but not focus order), resource loading,
 * visibility, onclick, tags, and tracks the size and shape of the view.
 */
@Implements(View.class)
public class ShadowView {
  public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

  @RealObject
  protected View realView;

  private View.OnClickListener onClickListener;
  private View.OnLongClickListener onLongClickListener;
  private View.OnFocusChangeListener onFocusChangeListener;
  private View.OnSystemUiVisibilityChangeListener onSystemUiVisibilityChangeListener;
  private boolean wasInvalidated;
  private View.OnTouchListener onTouchListener;
  protected AttributeSet attributeSet;
  public Point scrollToCoordinates = new Point();
  private boolean didRequestLayout;
  private MotionEvent lastTouchEvent;
  private float scaleX = 1.0f;
  private float scaleY = 1.0f;
  private int hapticFeedbackPerformed = -1;
  private boolean onLayoutWasCalled;
  private View.OnCreateContextMenuListener onCreateContextMenuListener;

  /**
   * Calls {@code performClick()} on a {@code View} after ensuring that it and its ancestors are visible and that it
   * is enabled.
   *
   * @param view the view to click on
   * @return true if {@code View.OnClickListener}s were found and fired, false otherwise.
   * @throws RuntimeException if the preconditions are not met.
   */
  public static boolean clickOn(View view) {
    return shadowOf(view).checkedPerformClick();
  }

  /**
   * Returns a textual representation of the appearance of the object.
   *
   * @param view the view to visualize
   * @return Textual representation of the appearance of the object.
   */
  public static String visualize(View view) {
    Canvas canvas = new Canvas();
    view.draw(canvas);
    return shadowOf(canvas).getDescription();
  }

  /**
   * Emits an xml-like representation of the view to System.out.
   *
   * @param view the view to dump
   */
  @SuppressWarnings("UnusedDeclaration")
  public static void dump(View view) {
    shadowOf(view).dump();
  }

  /**
   * Returns the text contained within this view.
   *
   * @param view the view to scan for text
   * @return Text contained within this view.
   */
  @SuppressWarnings("UnusedDeclaration")
  public static String innerText(View view) {
    return shadowOf(view).innerText();
  }

  public void __constructor__(Context context, AttributeSet attributeSet, int defStyle) {
    if (context == null) throw new NullPointerException("no context");

    this.attributeSet = attributeSet;

    invokeConstructor(View.class, realView, new ReflectionHelpers.ClassParameter(Context.class, context), new ReflectionHelpers.ClassParameter(AttributeSet.class, attributeSet),
        new ReflectionHelpers.ClassParameter(int.class, defStyle));
  }

  /**
   * Build drawable, either LayerDrawable or BitmapDrawable.
   *
   * @param resourceId Resource id
   * @return Drawable
   */
  protected Drawable buildDrawable(int resourceId) {
    return realView.getResources().getDrawable(resourceId);
  }

  protected String getQualifiers() {
    return shadowOf(realView.getResources().getConfiguration()).getQualifiers();
  }

  /**
   * Non-Android accessor.
   *
   * @return the resource ID of this view's background
   * @deprecated Use FEST assertions instead.
   */
  public int getBackgroundResourceId() {
    Drawable drawable = realView.getBackground();
    return drawable instanceof BitmapDrawable
        ? shadowOf(((BitmapDrawable) drawable).getBitmap()).getCreatedFromResId()
        : -1;
  }

  /**
   * Non-Android accessor.
   *
   * @return the color of this view's background, or 0 if it's not a solid color
   * @deprecated Use FEST assertions instead.
   */
  public int getBackgroundColor() {
    Drawable drawable = realView.getBackground();
    return drawable instanceof ColorDrawable ? ((ColorDrawable) drawable).getColor() : 0;
  }

  @HiddenApi
  @Implementation
  public void computeOpaqueFlags() {
  }

  @Implementation
  public void setOnFocusChangeListener(View.OnFocusChangeListener l) {
    onFocusChangeListener = l;
    directly().setOnFocusChangeListener(l);
  }

  @Implementation
  public void setOnClickListener(View.OnClickListener onClickListener) {
    this.onClickListener = onClickListener;
    directly().setOnClickListener(onClickListener);
  }

  @Implementation
  public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
    this.onLongClickListener = onLongClickListener;
    directly().setOnLongClickListener(onLongClickListener);
  }

  @Implementation
  public void setOnSystemUiVisibilityChangeListener(View.OnSystemUiVisibilityChangeListener onSystemUiVisibilityChangeListener) {
    this.onSystemUiVisibilityChangeListener = onSystemUiVisibilityChangeListener;
    directly().setOnSystemUiVisibilityChangeListener(onSystemUiVisibilityChangeListener);
  }

  @Implementation
  public void setOnCreateContextMenuListener(View.OnCreateContextMenuListener onCreateContextMenuListener) {
    this.onCreateContextMenuListener = onCreateContextMenuListener;
    directly().setOnCreateContextMenuListener(onCreateContextMenuListener);
  }

  @Implementation
  public void draw(android.graphics.Canvas canvas) {
    Drawable background = realView.getBackground();
    if (background != null) {
      shadowOf(canvas).appendDescription("background:");
      background.draw(canvas);
    }
  }

  @Implementation
  public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    onLayoutWasCalled = true;
    directlyOn(realView, View.class, "onLayout", new ReflectionHelpers.ClassParameter(boolean.class, changed), new ReflectionHelpers.ClassParameter(int.class, left),
        new ReflectionHelpers.ClassParameter(int.class, top), new ReflectionHelpers.ClassParameter(int.class, right), new ReflectionHelpers.ClassParameter(int.class, bottom));
  }

  public boolean onLayoutWasCalled() {
    return onLayoutWasCalled;
  }

  @Implementation
  public void requestLayout() {
    didRequestLayout = true;
    directly().requestLayout();
  }

  public boolean didRequestLayout() {
    return didRequestLayout;
  }

  public void setDidRequestLayout(boolean didRequestLayout) {
    this.didRequestLayout = didRequestLayout;
  }

  public void setViewFocus(boolean hasFocus) {
    if (onFocusChangeListener != null) {
      onFocusChangeListener.onFocusChange(realView, hasFocus);
    }
  }

  @Implementation
  public void invalidate() {
    wasInvalidated = true;
    directly().invalidate();
  }

  @Implementation
  public boolean onTouchEvent(MotionEvent event) {
    lastTouchEvent = event;
    return directly().onTouchEvent(event);
  }

  @Implementation
  public void setOnTouchListener(View.OnTouchListener onTouchListener) {
    this.onTouchListener = onTouchListener;
    directly().setOnTouchListener(onTouchListener);
  }

  public MotionEvent getLastTouchEvent() {
    return lastTouchEvent;
  }

  /**
   * Returns a string representation of this {@code View}. Unless overridden, it will be an empty string.
   *
   * <p>
   * Robolectric extension.
   * @return String representation of this view.
   */
  public String innerText() {
    return "";
  }

  /**
   * Dumps the status of this {@code View} to {@code System.out}
   */
  public void dump() {
    dump(System.out, 0);
  }

  /**
   * Dumps the status of this {@code View} to {@code System.out} at the given indentation level
   * @param out Output stream.
   * @param indent Indentation level.
   */
  public void dump(PrintStream out, int indent) {
    dumpFirstPart(out, indent);
    out.println("/>");
  }

  protected void dumpFirstPart(PrintStream out, int indent) {
    dumpIndent(out, indent);

    out.print("<" + realView.getClass().getSimpleName());
    dumpAttributes(out);
  }

  protected void dumpAttributes(PrintStream out) {
    if (realView.getId() > 0) {
      dumpAttribute(out, "id", shadowOf(realView.getContext()).getResourceLoader().getNameForId(realView.getId()));
    }

    switch (realView.getVisibility()) {
      case View.VISIBLE:
        break;
      case View.INVISIBLE:
        dumpAttribute(out, "visibility", "INVISIBLE");
        break;
      case View.GONE:
        dumpAttribute(out, "visibility", "GONE");
        break;
    }
  }

  protected void dumpAttribute(PrintStream out, String name, String value) {
    out.print(" " + name + "=\"" + (value == null ? null : TextUtils.htmlEncode(value)) + "\"");
  }

  protected void dumpIndent(PrintStream out, int indent) {
    for (int i = 0; i < indent; i++) out.print(" ");
  }

  /**
   * Non-Android accessor.
   *
   * @return whether or not {@link #invalidate()} has been called
   */
  public boolean wasInvalidated() {
    return wasInvalidated;
  }

  /**
   * Clears the wasInvalidated flag
   */
  public void clearWasInvalidated() {
    wasInvalidated = false;
  }

  /**
   * Utility method for clicking on views exposing testing scenarios that are not possible when using the actual app.
   *
   * @throws RuntimeException if the view is disabled or if the view or any of its parents are not visible.
   * @return Return value of the underlying click operation.
   */
  public boolean checkedPerformClick() {
    if (!realView.isShown()) {
      throw new RuntimeException("View is not visible and cannot be clicked");
    }
    if (!realView.isEnabled()) {
      throw new RuntimeException("View is not enabled and cannot be clicked");
    }

    return realView.performClick();
  }

  /**
   * Non-android accessor.
   *
   * @return Touch listener, if set.
   */
  public View.OnTouchListener getOnTouchListener() {
    return onTouchListener;
  }

  /**
   * Non-android accessor.
   *
   * @return Returns click listener, if set.
   */
  public View.OnClickListener getOnClickListener() {
    return onClickListener;
  }

  /**
   * Non-android accessor.
   *
   * @return Returns long click listener, if set.
   */
  public View.OnLongClickListener getOnLongClickListener() {
    return onLongClickListener;
  }

  /**
   * Non-android accessor.
   *
   * @return Returns system ui visibility change listener.
   */
  public View.OnSystemUiVisibilityChangeListener getOnSystemUiVisibilityChangeListener() {
    return onSystemUiVisibilityChangeListener;
  }

  /**
   * Non-android accessor.
   *
   * @return Returns create ContextMenu listener, if set.
   */
  public View.OnCreateContextMenuListener getOnCreateContextMenuListener() {
    return onCreateContextMenuListener;
  }

  @Implementation
  public Bitmap getDrawingCache() {
    return ReflectionHelpers.callConstructorReflectively(Bitmap.class);
  }

  @Implementation
  public void post(Runnable action) {
    ShadowLooper.getUiThreadScheduler().post(action);
  }

  @Implementation
  public void postDelayed(Runnable action, long delayMills) {
    ShadowLooper.getUiThreadScheduler().postDelayed(action, delayMills);
  }

  @Implementation
  public void postInvalidateDelayed(long delayMilliseconds) {
    ShadowLooper.getUiThreadScheduler().postDelayed(new Runnable() {
      @Override
      public void run() {
        realView.invalidate();
      }
    }, delayMilliseconds);
  }

  @Implementation
  public void removeCallbacks(Runnable callback) {
    shadowOf(Looper.getMainLooper()).getScheduler().remove(callback);
  }

  @Implementation
  public void scrollTo(int x, int y) {
    try {
      Method method = View.class.getDeclaredMethod("onScrollChanged", new Class[]{int.class, int.class, int.class, int.class});
      method.setAccessible(true);
      method.invoke(realView, x, y, scrollToCoordinates.x, scrollToCoordinates.y);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    scrollToCoordinates = new Point(x, y);
  }

  @Implementation
  public int getScrollX() {
    return scrollToCoordinates != null ? scrollToCoordinates.x : 0;
  }

  @Implementation
  public int getScrollY() {
    return scrollToCoordinates != null ? scrollToCoordinates.y : 0;
  }

  @Implementation
  public void setScrollX(int scrollX) {
    scrollTo(scrollX, scrollToCoordinates.y);
  }

  @Implementation
  public void setScrollY(int scrollY) {
    scrollTo(scrollToCoordinates.x, scrollY);
  }

  @Implementation
  public void setScaleX(float scaleX) {
    this.scaleX = scaleX;
  }

  @Implementation
  public float getScaleX() {
    return scaleX;
  }

  @Implementation
  public void setScaleY(float scaleY) {
    this.scaleY = scaleY;
  }

  @Implementation
  public float getScaleY() {
    return scaleY;
  }

  @Implementation
  public void onAnimationEnd() {
  }

  /*
   * Non-Android accessor.
   */
  public void finishedAnimation() {
    try {
      Method onAnimationEnd = realView.getClass().getDeclaredMethod("onAnimationEnd", new Class[0]);
      onAnimationEnd.setAccessible(true);
      onAnimationEnd.invoke(realView);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  public boolean isAttachedToWindow() {
    return ReflectionHelpers.getFieldReflectively(realView, "mAttachInfo") != null;
  }

  public void callOnAttachedToWindow() {
    invokeReflectively("onAttachedToWindow");
  }

  public void callOnDetachedFromWindow() {
    invokeReflectively("onDetachedFromWindow");
  }

  private void invokeReflectively(String methodName) {
    ReflectionHelpers.callInstanceMethodReflectively(realView, methodName);
  }

  @Implementation
  public boolean performHapticFeedback(int hapticFeedbackType) {
    hapticFeedbackPerformed = hapticFeedbackType;
    return true;
  }

  public int lastHapticFeedbackPerformed() {
    return hapticFeedbackPerformed;
  }

  public void setMyParent(ViewParent viewParent) {
    directlyOn(realView, View.class, "assignParent", new ReflectionHelpers.ClassParameter(ViewParent.class, viewParent));
  }

  private View directly() {
    return directlyOn(realView, View.class);
  }
}
