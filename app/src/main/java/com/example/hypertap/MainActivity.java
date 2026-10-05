package com.example.hypertap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(48, 80, 48, 40);
        TextView title = new TextView(this);
        title.setText("HyperTap\n\nİki noktaya sırayla hızlı dokunma test aracı");
        title.setTextSize(24); title.setTextColor(Color.BLACK); title.setGravity(Gravity.CENTER);
        Button enable = new Button(this); enable.setText("ERİŞİLEBİLİRLİK İZNİNİ AÇ");
        enable.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        TextView help = new TextView(this);
        help.setText("\n1. Listeden HyperTap'i etkinleştir.\n2. Kırmızı İ ve mavi G hedeflerini sürükle.\n3. TURBO için 0 bırakıp BAŞLAT'a bas.");
        help.setTextSize(17); help.setTextColor(Color.DKGRAY);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        root.addView(enable, new LinearLayout.LayoutParams(-1, -2));
        root.addView(help, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
    }
}
