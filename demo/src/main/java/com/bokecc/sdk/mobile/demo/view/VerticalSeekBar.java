package com.bokecc.sdk.mobile.demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.AbsSeekBar;

/**
 * 
 * @author CC视频
 * 
 * VerticalSeekBar允许用户改变滑块外观，通过指定一个drawable对象，将对象作为自定义滑块。
 * 
 * 为了响应滑块位置的改变，可以绑定OnSeekBarChangeListener来进行相应的监听处理。
 * 
 */
public class VerticalSeekBar extends AbsSeekBar {
	
	public interface OnSeekBarChangeListener {
		
		public abstract void onProgressChanged(VerticalSeekBar verticalseekbar, int i, boolean flag);

		public abstract void onStartTrackingTouch(VerticalSeekBar verticalseekbar);

		public abstract void onStopTrackingTouch(VerticalSeekBar verticalseekbar);
	}

	private int height;
	private OnSeekBarChangeListener onSeekBarChangeListener;
	private Drawable thumb;
	private int width;

	public VerticalSeekBar(Context context) {
		this(context, null);
	}

	public VerticalSeekBar(Context context, AttributeSet attributeset) {
		this(context, attributeset, 0x101007b);
	}

	public VerticalSeekBar(Context context, AttributeSet attributeset, int i) {
		super(context, attributeset, i);
	}

	// 让父类不用onInterceptTouchEvent(MotionEvent)来拦截触屏事件。true表示child不让父类拦截触屏事件
	private void attemptClaimDrag() {
		if (getParent() != null)
			getParent().requestDisallowInterceptTouchEvent(true);
	}

	/**
	 * 设置滑块的进度值
	 * 
	 * @param height
	 * @param drawable
	 * @param scale
	 * @param gap
	 */
	private void setThumbPos(int height, Drawable drawable, float scale, int gap) {
		int top = (height + getPaddingTop()) - getPaddingBottom();
		int drawableWidth = drawable.getIntrinsicWidth();
		int drawableHeight = drawable.getIntrinsicHeight();
		
		int left = (int) (scale * (float) ((top - drawableWidth) + 2 * getThumbOffset()));
		if (gap != 0x80000000) {
			top = gap;
			drawableHeight = gap + drawableHeight;
		} else {
			Rect rect = drawable.getBounds();
			top = rect.top;
			drawableHeight = rect.bottom;
		}
		
		int offset = drawableWidth / 2;
		drawable.setBounds(left - offset, top, left + offset, drawableHeight);
	}

	/**
	 * 监听触摸拖动事件
	 * 
	 * @param motionevent
	 */
	private void trackTouchEvent(MotionEvent motionevent) {
		int h = getHeight();
		int w = h - getPaddingBottom() - getPaddingTop();
		float y = (int) motionevent.getY();
		if (y <= h - getPaddingBottom()) {
			if (y >= getPaddingTop())
				y = (float) (h - getPaddingBottom() - y) / (float) w;
			else
				y = 1F;
		} else {
			y = 0F;
		}
		setProgress((int) (y * (float) getMax()));// 设置进度条
	}

	@SuppressWarnings("deprecation")
	public boolean dispatchKeyEvent(KeyEvent keyevent) {
		boolean flag;
		KeyEvent keyEvent;
		if (keyevent.getAction() != 0) {
			flag = false;
		} else {
			switch (keyevent.getKeyCode()) {
			default:
				keyEvent = new KeyEvent(0, keyevent.getKeyCode());
				break;
			case 19:
				keyEvent = new KeyEvent(0, 22);
				break;
			case 20:
				keyEvent = new KeyEvent(0, 21);
				break;
			case 21:
				keyEvent = new KeyEvent(0, 20);
				break;
			case 22:
				keyEvent = new KeyEvent(0, 19);
				break;
			}
			flag = keyEvent.dispatch(this);
		}
		return flag;
	}

	protected void onDraw(Canvas canvas) {
		canvas.rotate(-90F);
		canvas.translate(-height, 0F);
		super.onDraw(canvas);
	}

	protected void onMeasure(int i, int j) {
		height = android.view.View.MeasureSpec.getSize(j);
		width = android.view.View.MeasureSpec.getSize(i);
		setMeasuredDimension(width, height);
	}

	void onProgressRefresh(float f, boolean flag) {
		Drawable drawable = thumb;
		if (drawable != null) {
			setThumbPos(getHeight(), drawable, f, 0x80000000);
			invalidate();
		}
		if (onSeekBarChangeListener != null)
			onSeekBarChangeListener.onProgressChanged(this, getProgress(), flag);
	}

	protected void onSizeChanged(int i, int j, int k, int l) {
		super.onSizeChanged(j, i, k, l);
	}

	void onStartTrackingTouch() {
		if (onSeekBarChangeListener != null)
			onSeekBarChangeListener.onStartTrackingTouch(this);
	}

	void onStopTrackingTouch() {
		if (onSeekBarChangeListener != null)
			onSeekBarChangeListener.onStopTrackingTouch(this);
	}

	public boolean onTouchEvent(MotionEvent motionevent) {
		boolean flag;
		if (isEnabled()) {
			switch (motionevent.getAction()) {
			case 0:
				setPressed(true);
				onStartTrackingTouch();
				trackTouchEvent(motionevent);
				break;
			case 1:
				trackTouchEvent(motionevent);
				onStopTrackingTouch();
				setPressed(false);
				break;
			case 2:
				trackTouchEvent(motionevent);
				attemptClaimDrag();
				break;
			case 3:
				onStopTrackingTouch();
				setPressed(false);
				break;
			}
			flag = true;
		} else {
			flag = false;
		}
		return flag;
	}

	public void setOnSeekBarChangeListener(
			OnSeekBarChangeListener onseekbarchangelistener) {
		onSeekBarChangeListener = onseekbarchangelistener;
	}

	public void setThumb(Drawable drawable) {
		thumb = drawable;
		super.setThumb(drawable);
	}
}
