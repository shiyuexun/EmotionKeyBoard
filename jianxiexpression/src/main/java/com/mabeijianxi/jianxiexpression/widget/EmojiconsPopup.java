
package com.mabeijianxi.jianxiexpression.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.PopupWindow;

import com.mabeijianxi.jianxiexpression.ExpressionGridFragment;
import com.mabeijianxi.jianxiexpression.ExpressionItemAdapter;
import com.mabeijianxi.jianxiexpression.R;
import com.mabeijianxi.jianxiexpression.core.ExpressionCache;
import com.mabeijianxi.jianxiexpression.core.ExpressionTransformEngine;


/**
 * @author Hani Al Momani (hani.momanii@gmail.com)
 */

public class EmojiconsPopup extends PopupWindow implements AdapterView.OnItemClickListener {
    private PagerAdapter mEmojisAdapter;
    private int keyBoardHeight = 0;
    private Boolean pendingOpen = false;
    private Boolean isOpened = false;
    private ExpressionItemAdapter expressionItemAdapter;
    public ExpressionGridFragment.ExpressionClickListener onEmojiconClickedListener;
    public ExpressionGridFragment.ExpressionDeleteClickListener onEmojiconDeleteListener;
    OnSoftKeyboardOpenCloseListener onSoftKeyboardOpenCloseListener;
    View rootView;
    Context mContext;
    View view;
    boolean setColor = false;
    String iconPressedColor = "#495C66";
    String backgroundColor = "#E6EBEF";
    private String[] mExpressionName;

    /**
     * Constructor
     *
     * @param rootView         The top most layout in your view hierarchy. The difference of this view and the screen height will be used to calculate the keyboard height.
     * @param mContext         The context of current activity.
     * @param iconPressedColor .
     * @param backgroundColor  .
     */
    public EmojiconsPopup(View rootView, Context mContext, String iconPressedColor, String backgroundColor) {
        super(mContext);
        this.setColor = true;
        this.backgroundColor = backgroundColor;
        this.iconPressedColor = iconPressedColor;
        this.mContext = mContext;
        this.rootView = rootView;
        this.mExpressionName = ExpressionCache.page_1;
        View customView = createCustomView();
        setContentView(customView);
        setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setSize(LayoutParams.MATCH_PARENT, 255);
        setBackgroundDrawable(null);


    }

    /**
     * Constructor
     *
     * @param rootView The top most layout in your view hierarchy. The difference of this view and the screen height will be used to calculate the keyboard height.
     * @param mContext The context of current activity.
     */
    public EmojiconsPopup(View rootView, Context mContext) {
        super(mContext);
        this.mContext = mContext;
        this.rootView = rootView;
        this.mExpressionName = ExpressionCache.page_1;
        View customView = createCustomView();
        setContentView(customView);
        setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setSize(LayoutParams.MATCH_PARENT, 255);
        setBackgroundDrawable(null);
    }

    /**
     * Set the listener for the event of keyboard opening or closing.
     */
    public void setOnSoftKeyboardOpenCloseListener(OnSoftKeyboardOpenCloseListener listener) {
        this.onSoftKeyboardOpenCloseListener = listener;
    }

    /**
     * Set the listener for the event when any of the emojicon is clicked
     */
    public void setOnEmojiconClickedListener(ExpressionGridFragment.ExpressionClickListener listener) {
        this.onEmojiconClickedListener = listener;
    }

    /**
     * Set the listener for the event when backspace on emojicon popup is clicked
     */
    public void setonEmojiconDeleteListener(ExpressionGridFragment.ExpressionDeleteClickListener listener) {
        this.onEmojiconDeleteListener = listener;
    }

    /**
     * Use this function to show the emoji popup.
     * NOTE: Since, the soft keyboard sizes are variable on different android devices, the
     * library needs you to open the soft keyboard atleast once before calling this function.
     * If that is not possible see showAtBottomPending() function.
     */
    public void showAtBottom() {
        showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }

    /**
     * Use this function when the soft keyboard has not been opened yet. This
     * will show the emoji popup after the keyboard is up next time.
     * Generally, you will be calling InputMethodManager.showSoftInput function after
     * calling this function.
     */
    public void showAtBottomPending() {
        if (isKeyBoardOpen())
            showAtBottom();
        else
            pendingOpen = true;
    }

    /**
     * @return Returns true if the soft keyboard is open, false otherwise.
     */
    public Boolean isKeyBoardOpen() {
        return isOpened;
    }

    /**
     * Dismiss the popup
     */
    @Override
    public void dismiss() {
        super.dismiss();
    }

    /**
     * Call this function to resize the emoji popup according to your soft keyboard size
     */
    public void setSizeForSoftKeyboard() {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                int screenHeight = getUsableScreenHeight();
                int heightDifference = screenHeight
                        - (r.bottom - r.top);
                int resourceId = mContext.getResources()
                        .getIdentifier("status_bar_height",
                                "dimen", "android");
                if (resourceId > 0) {
                    heightDifference -= mContext.getResources()
                            .getDimensionPixelSize(resourceId);
                }
                if (heightDifference > 100) {
                    keyBoardHeight = heightDifference;
                    setSize(LayoutParams.MATCH_PARENT, keyBoardHeight);
                    if (isOpened == false) {
                        if (onSoftKeyboardOpenCloseListener != null)
                            onSoftKeyboardOpenCloseListener.onKeyboardOpen(keyBoardHeight);
                    }
                    isOpened = true;
                    if (pendingOpen) {
                        showAtBottom();
                        pendingOpen = false;
                    }
                } else {
                    isOpened = false;
                    if (onSoftKeyboardOpenCloseListener != null)
                        onSoftKeyboardOpenCloseListener.onKeyboardClose();
                }
            }
        });
    }

    private int getUsableScreenHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();

            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);

            return metrics.heightPixels;

        } else {
            return rootView.getRootView().getHeight();
        }
    }

    /**
     * Manually set the popup window size
     *
     * @param width  Width of the popup
     * @param height Height of the popup
     */
    public void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }


    private View createCustomView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.my_expression_gridview, null, false);
        CustomGridView gv_expression = (CustomGridView) view.findViewById(R.id.gv_expression);
        expressionItemAdapter = new ExpressionItemAdapter(mContext, mExpressionName);
        gv_expression.setAdapter(expressionItemAdapter);
        gv_expression.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 20) {
            if (onEmojiconDeleteListener != null) {
                onEmojiconDeleteListener.expressionDeleteClick(view);
            }
            return;
        }

        String itemAtPosition = (String) parent.getItemAtPosition(position);
        if (!TextUtils.isEmpty(itemAtPosition) && onEmojiconClickedListener != null) {
            onEmojiconClickedListener.expressionClick(itemAtPosition);
        }
    }


    /**
     * A class, that can be used as a TouchListener on any view (e.g. a Button).
     * It cyclically runs a clickListener, emulating keyboard-like behaviour. First
     * click is fired immediately, next before initialInterval, and subsequent before
     * normalInterval.
     * <p/>
     * <p>Interval is scheduled before the onClick completes, so it has to run fast.
     * If it runs slow, it does not generate skipped onClicks.
     */
    public static class RepeatListener implements View.OnTouchListener {

        private Handler handler = new Handler();

        private int initialInterval;
        private final int normalInterval;
        private final OnClickListener clickListener;

        private Runnable handlerRunnable = new Runnable() {
            @Override
            public void run() {
                if (downView == null) {
                    return;
                }
                handler.removeCallbacksAndMessages(downView);
                handler.postAtTime(this, downView, SystemClock.uptimeMillis() + normalInterval);
                clickListener.onClick(downView);
            }
        };

        private View downView;

        /**
         * @param initialInterval The interval before first click event
         * @param normalInterval  The interval before second and subsequent click
         *                        events
         * @param clickListener   The OnClickListener, that will be called
         *                        periodically
         */
        public RepeatListener(int initialInterval, int normalInterval, OnClickListener clickListener) {
            if (clickListener == null)
                throw new IllegalArgumentException("null runnable");
            if (initialInterval < 0 || normalInterval < 0)
                throw new IllegalArgumentException("negative interval");

            this.initialInterval = initialInterval;
            this.normalInterval = normalInterval;
            this.clickListener = clickListener;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downView = view;
                    handler.removeCallbacks(handlerRunnable);
                    handler.postAtTime(handlerRunnable, downView, SystemClock.uptimeMillis() + initialInterval);
                    clickListener.onClick(view);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    handler.removeCallbacksAndMessages(downView);
                    downView = null;
                    return true;
            }
            return false;
        }
    }


    public interface OnSoftKeyboardOpenCloseListener {
        void onKeyboardOpen(int keyBoardHeight);

        void onKeyboardClose();
    }


}