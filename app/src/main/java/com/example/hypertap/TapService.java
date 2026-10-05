package com.example.hypertap;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TapService extends AccessibilityService {
    private WindowManager wm;
    private View panel, forward, back;
    private WindowManager.LayoutParams forwardParams, backParams;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean running, nextForward = true;
    private long intervalMs = 0;

    @Override public void onServiceConnected() { showOverlay(); }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { stop(); }
    @Override public void onDestroy() { stop(); remove(panel); remove(forward); remove(back); super.onDestroy(); }

    private void showOverlay() {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        forward = marker("İ", Color.RED);
        back = marker("G", Color.rgb(0, 100, 255));
        forwardParams = params(64, 64, 100, 400);
        backParams = params(64, 64, 300, 400);
        makeDraggable(forward, forwardParams); makeDraggable(back, backParams);
        wm.addView(forward, forwardParams); wm.addView(back, backParams);

        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(12, 10, 12, 10); box.setBackgroundColor(0xDD222222);
        TextView label = new TextView(this); label.setText("Gecikme (ms) • 0 = TURBO"); label.setTextColor(Color.WHITE);
        EditText delay = new EditText(this); delay.setText("0"); delay.setTextColor(Color.WHITE); delay.setInputType(2);
        Button toggle = new Button(this); toggle.setText("BAŞLAT");
        toggle.setOnClickListener(v -> {
            if (running) { stop(); toggle.setText("BAŞLAT"); return; }
            try { intervalMs = Math.max(0, Math.min(1000, Long.parseLong(delay.getText().toString()))); }
            catch (Exception e) { intervalMs = 0; delay.setText("0"); }
            running = true; nextForward = true;
            forward.setVisibility(View.GONE); back.setVisibility(View.GONE);
            toggle.setText("DURDUR"); tick();
        });
        box.addView(label); box.addView(delay); box.addView(toggle);
        panel = box;
        WindowManager.LayoutParams pp = params(260, -2, 20, 80);
        makeDraggable(panel, pp); wm.addView(panel, pp);
    }

    private TextView marker(String text, int color) {
        TextView v = new TextView(this); v.setText(text); v.setTextColor(Color.WHITE); v.setTextSize(23);
        v.setGravity(Gravity.CENTER); v.setBackgroundColor(color); return v;
    }

    private WindowManager.LayoutParams params(int w, int h, int x, int y) {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(w, h,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START; p.x = x; p.y = y; return p;
    }

    private void makeDraggable(View v, WindowManager.LayoutParams p) {
        v.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY; int startX, startY;
            public boolean onTouch(View view, MotionEvent e) {
                if (running && (view == forward || view == back)) return true;
                if (e.getAction() == MotionEvent.ACTION_DOWN) { downX=e.getRawX(); downY=e.getRawY(); startX=p.x; startY=p.y; return true; }
                if (e.getAction() == MotionEvent.ACTION_MOVE) { p.x=startX+(int)(e.getRawX()-downX); p.y=startY+(int)(e.getRawY()-downY); wm.updateViewLayout(view,p); return true; }
                return true;
            }
        });
    }

    private void tick() {
        if (!running) return;
        WindowManager.LayoutParams p = nextForward ? forwardParams : backParams;
        Path path = new Path(); path.moveTo(p.x + 32, p.y + 32);
        GestureDescription g = new GestureDescription.Builder()
            .addStroke(new GestureDescription.StrokeDescription(path, 0, 1)).build();
        dispatchGesture(g, new GestureResultCallback() {
            @Override public void onCompleted(GestureDescription d) { nextForward = !nextForward; scheduleNext(); }
            @Override public void onCancelled(GestureDescription d) { if (running) scheduleNext(); }
        }, null);
    }

    private void scheduleNext() {
        if (intervalMs == 0) handler.post(this::tick);
        else handler.postDelayed(this::tick, intervalMs);
    }

    private void stop() {
        running = false; handler.removeCallbacksAndMessages(null);
        if (forward != null) forward.setVisibility(View.VISIBLE);
        if (back != null) back.setVisibility(View.VISIBLE);
    }
    private void remove(View v) { if (v != null && wm != null) try { wm.removeView(v); } catch (Exception ignored) {} }
}
