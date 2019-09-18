package com.whatzwhat.hashlabeldemo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Choreographer;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.util.Consumer;

import java.util.Random;

public class HashTextView extends AppCompatTextView {

    enum Mode {
        NORMAL, NUMBER, EN, ZH
    }

    enum InitialPoint {
        FIRST, LAST
    }

    // Random
    private Random random = new Random();
    // DisplayLink
    private DisplayLink displayLink = null;

    // 生成方向
    InitialPoint initPoint = InitialPoint.FIRST;
    // 亂碼模式
    Mode mode = Mode.NORMAL;

    // 文字
    private String content = "";
    // 動畫時間
    private long totalTime = 300; // 1000毫秒 = 1秒
    // 進行時間
    private long progress = 0;
    // 開始時間
    private long startTime = 0;
    // 動畫變化區間
    private long sectionTime = 0;


    public HashTextView(Context context) {
        super(context);
    }

    public HashTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HashTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setText(String text, Double duration, Mode mode) {
        this.mode = mode;
        setText(text, duration);
    }

    public void setText(String text, Double duration) {
        resetProperty();
        recycleDisplayLink();
        totalTime = millisecond(duration);
        content = text;
        startTime = System.currentTimeMillis();
        sectionTime = totalTime / content.length();
        addDisplayLink();
    }


    // Second to Millisecond
    private long millisecond(Double second) {
        return (long)(second * 1000);
    }

    // Random number
    private int random(int from, int to) {
        if (null == random) { random = new Random(); }
        return random.ints(from, to + 1).findFirst().getAsInt();
    }

    // Unicode decode
    private String uniDecode(int unicode) {
        return String.valueOf(Character.toChars(unicode));
    }

    // 根據模式取得亂碼字串.
    private String randomText(Mode mode) {
        int number ;
        switch (mode) {
            case NORMAL:
                number = random(33, 126);
                break;
            case NUMBER:
                number = random(48, 57);
                break;
            case EN:
                number = random(65, 122);
                while (90 < number && number < 97) { number = random(65, 122); }
                break;
            case ZH:
                number = random(13312, 40911);
                break;
            default:
                number = random(33, 40911);
                while (126 < number && number < 13312) { number = random(65, 122); }
                break;
        }
        return uniDecode(number);
    }

    // Loop ...
    private void updateValue() {

        long now = System.currentTimeMillis();
        progress = now - startTime;

        if (progress >= totalTime) {
            recycleDisplayLink();
        }

        String text = "";
        long randomSize = content.length() - (progress / sectionTime);
        long textSize = content.length() - randomSize;

        for (long i = 0; i < randomSize; i++) {
            text += randomText(mode);
        }

        switch (initPoint) {
            case FIRST:
                text = content.substring(0, (int)textSize) + text;
                break ;
            case LAST:
                text = text + content.substring(content.length()-(int)textSize, content.length());
                break ;
        }

        StringBuffer stringBuffer = new StringBuffer(text);

        this.post(new Runnable() {
            @Override
            public void run() {
                setViewText(stringBuffer);
            }
        });
    }

    private void setViewText(StringBuffer text) {
        this.setText(text);
    }

    private void addDisplayLink() {
        displayLink = new DisplayLink((timeNano) -> {
            updateValue();
        });
        displayLink.start();
    }

    private void recycleDisplayLink() {
        if (null != displayLink) {
            displayLink.signal();
            displayLink = null;
        }
    }

    private void resetProperty() {
        content = "";
        progress = 0;
        startTime = 0;
        sectionTime = 0;
    }

    // DisplayLink core.
    private class DisplayLink extends Thread implements Choreographer.FrameCallback {

        private volatile Handler signal;
        private Consumer<Long> update;

        public DisplayLink(Consumer<Long> update) {
            super();
            this.update = update;
        }

        @Override
        public void run() {
            setName("HashTextViewThread");
            Looper.prepare();
            signal = new Handler() {
                public void handleMessage(Message msg) {
                    Looper.myLooper().quit();
                }
            };
            Choreographer.getInstance().postFrameCallback(this);
            Looper.loop();
            Choreographer.getInstance().removeFrameCallback(this);
        }

        public void signal() {
            if (this.signal != null) {
                this.signal.sendEmptyMessage(0);
                this.signal = null;
            }
        }

        @Override
        public void doFrame(long timeNano) {
            if (this.update != null) {
                update.accept(timeNano);
            }
            Choreographer.getInstance().postFrameCallback(this);
        }

    }

}
