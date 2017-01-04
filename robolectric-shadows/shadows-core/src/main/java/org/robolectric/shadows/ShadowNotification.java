package org.robolectric.shadows;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.shadows.ResourceHelper.getInternalResourceId;

/**
 * Shadow for {@link android.app.Notification}.
 */
@Implements(Notification.class)
public class ShadowNotification {

  @RealObject
  Notification realNotification;

  public CharSequence getContentTitle() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getString(Notification.EXTRA_TITLE)
        : findText(applyContentView(), "title");
  }

  public CharSequence getContentText() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getString(Notification.EXTRA_TEXT)
        : findText(applyContentView(), "text");
  }

  public CharSequence getContentInfo() {
    if (getApiLevel() >= N) {
      return realNotification.extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
    } else {
      String resourceName = getApiLevel() >= N ? "header_text" : "info";
      return findText(applyContentView(), resourceName);
    }
  }

  public boolean isOngoing() {
    return ((realNotification.flags & Notification.FLAG_ONGOING_EVENT) == Notification.FLAG_ONGOING_EVENT);
  }

  public CharSequence getBigText() {
    if (getApiLevel() >= N) {
      return realNotification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
    } else {
      return findText(applyBigContentView(), "big_text");
    }
  }

  public CharSequence getBigContentTitle() {
    if (getApiLevel() >= N) {
      return realNotification.extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
    } else {
      return findText(applyBigContentView(), "title");
    }
  }

  public CharSequence getBigContentText() {
    if (getApiLevel() >= N) {
      return realNotification.extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT);
    } else {
      String resourceName = getApiLevel() >= N ? "header_text" : "text";
      return findText(applyBigContentView(), resourceName);
    }
  }

  public Bitmap getBigPicture() {
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N) {
      return realNotification.extras.getParcelable(Notification.EXTRA_PICTURE);
    } else {
      ImageView imageView = (ImageView) applyBigContentView().findViewById(getInternalResourceId("big_picture"));
      return imageView != null && imageView.getDrawable() != null
          ? ((BitmapDrawable) imageView.getDrawable()).getBitmap() : null;
    }
  }

  public boolean isWhenShown() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getBoolean(Notification.EXTRA_SHOW_WHEN)
        : findView(applyContentView(), "chronometer").getVisibility() == View.VISIBLE
        || findView(applyContentView(), "time").getVisibility() == View.VISIBLE;
  }

  /**
   * This method does not work on API > {@link Build.VERSION_CODES#N}
   * @deprecated Use {@link #isIndeterminate()}, {@link #getMax()} ()}, {@link #getProgress()} ()} instead.
   */
  @Deprecated
  public ProgressBar getProgressBar() {
    return ((ProgressBar) findView(applyContentView(), "progress"));
  }

  public boolean isIndeterminate() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE)
        : getProgressBar().isIndeterminate();
  }

  public int getMax() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getInt(Notification.EXTRA_PROGRESS_MAX)
        : getProgressBar().getMax();
  }

  public int getProgress() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getInt(Notification.EXTRA_PROGRESS)
        : getProgressBar().getProgress();
  }

  public boolean usesChronometer() {
    return RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N
        ? realNotification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER)
        : findView(applyContentView(), "chronometer").getVisibility() == View.VISIBLE;
  }

  private View applyContentView() {
    return realNotification.contentView.apply(RuntimeEnvironment.application, new FrameLayout(RuntimeEnvironment.application));
  }

  private View applyBigContentView() {
    return realNotification.bigContentView.apply(RuntimeEnvironment.application, new FrameLayout(RuntimeEnvironment.application));
  }

  private CharSequence findText(View view, String resourceName) {
    TextView textView = (TextView) findView(view, resourceName);
    return textView.getText();
  }

  private View findView(View view, String resourceName) {
    View subView = view.findViewById(getInternalResourceId(resourceName));
    if (subView == null) {
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      Shadows.shadowOf(view).dump(new PrintStream(buf), 4);
      throw new IllegalArgumentException("no id." + resourceName + " found in view:\n" + buf.toString());
    }
    return subView;
  }
}
