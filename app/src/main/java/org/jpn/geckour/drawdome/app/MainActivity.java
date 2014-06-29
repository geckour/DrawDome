package org.jpn.geckour.drawdome.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ActionBarActivity {
    SharedPreferences sp;
    int min = 4;
    int max = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getString("min_vertices", "null").equals("null") || sp.getString("min_vertices", "null") == null) {
            sp.edit().putString("min_vertices", "4").apply();
        }
        try {
            min = Integer.parseInt(sp.getString("min_vertices", "4"));
        } catch (Exception e) {
            Log.v("error", "Can't translate minimum-vertices to int.");
        }
        if (sp.getString("max_vertices", "null").equals("null") || sp.getString("max_vertices", "null") == null) {
            sp.edit().putString("max_vertices", "80").apply();
        }
        try {
            max = Integer.parseInt(sp.getString("max_vertices", "80"));
        } catch (Exception e) {
            Log.v("error", "Can't translate maximum-vertices to int.");
        }

        MyView view = new MyView(this);
        setContentView(view);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, Pref.class), 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //他classへのintentの戻り値を受け付ける
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //Pref.javaからの戻り値の場合
        if (requestCode == 0){
            if (resultCode == Activity.RESULT_OK) {
                if (Integer.parseInt(sp.getString("min_vertices", "4")) < 4) {
                    sp.edit().putString("min_vertices", "4");
                }
                if (Integer.parseInt(sp.getString("max_vertices", "80")) > 100) {
                    sp.edit().putString("max_vertices", "100");
                }
            }
        }
    }

    class MyView extends View {
        int framec = 0;

        int r = 0, g = 0, b = 0;
        int randomColor;
        int black = Color.rgb(0, 0, 0), white = Color.rgb(255, 255, 255);
        int bgColor = (Math.random() <= 0.5) ? black : white;

        int l = (int) (min + Math.random() * (max - min + 1));
        int order = 1;
        double speed = 2 + Math.random() * 4;
        double radius;
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        double offsetX, offsetY;
        double cr = (Math.PI * 2) / l;
        double[] pX, pY;
        CalcDist[] dists;
        int step = 0;
        int interval = ((int) (45 / speed) >= 10) ? (int) (45 / speed) : 10;
        boolean cont[] = new boolean[l / 2 + 3];
        int reachfX[] = new int[l / 2 + 3];
        int reachfY[] = new int[l / 2 + 3];

        Paint p = new Paint();

        boolean state = true;
        boolean forward = true;

        private final Handler handler = new Handler();

        public MyView(Context context) {
            super(context);

            display.getSize(point);
            offsetX = point.x / 2;
            offsetY = point.y / 2 - 50;
            radius = offsetX < offsetY ? offsetX * 0.9 : offsetY * 0.9;

            p.setColor(randomColor);

            //randomColorと背景色(黒/白)のコントラストが低い場合に再計算
            int limitL = 80, limitU = 170;
            while ((r < limitL && g < limitL) || (g < limitL && b < limitL) || (b < limitL && r < limitL) || (limitU < r && limitU < g) || (limitU < g && limitU < b) || (limitU < b && limitU < r)) {
                r = (int) (Math.random() * 256);
                g = (int) (Math.random() * 256);
                b = (int) (Math.random() * 256);
            }
            randomColor = Color.argb(90, r, g, b);

            pX = new double[l];
            pY = new double[l];
            for (int i = 0; i < l; i++) {
                pX[i] = Math.cos(cr * i) * radius + offsetX;
                pY[i] = Math.sin(cr * i) * radius + offsetY;
                BigDecimal bdX = new BigDecimal(pX[i]), bdY = new BigDecimal(pY[i]);
                Log.v("echo", "pX[" + String.format("%03d", i) + "]:" + String.format("%5f", bdX.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue()) + "\tpY[" + String.format("%03d", i) + "]:" + String.format("%5f", bdY.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue()));
            }
            for (int i = 0; i < l / 2; i++) {
                cont[i] = true;
            }
            for (int i = 0; i < l / 2 + 3; i++) {
                reachfX[i] = 0;
                reachfY[i] = 0;
            }
            for (int i = 0; i < String.valueOf(l).length(); i++) order *= 10;
            dists = new CalcDist[(l / 4 + 1) * order];

            Timer timer = new Timer(false);
            timer.schedule(new TimerTask() {
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            if (state) {
                                if (forward) framec++;
                                else framec--;
                                //再描画
                                invalidate();
                            }
                        }
                    });
                }
            }, 100, 24);
        }

        @Override
        public void onDraw(Canvas c) {
            c.drawColor(bgColor);

            for (int i = 0; i < l; i++) {
                for (int j = 2; j <= step; j += 2) {
                    int p1, p2;
                    //点1と点2を定義
                    if (i % l < l - j) {
                        p1 = i % l;
                        p2 = i % l + j;
                    } else {
                        p1 = i % l;
                        p2 = i % l + j - (l);
                    }
                    double sumpX2 = pX[p1], sumpY2 = pY[p1];
                    //線の長さを定義、引き終わっていれば長さを固定
                    if (step != 0) {
                        int dist_i = (j / 2) * order + i;

                        if (dists[dist_i] == null) {
                            CalcDist calcDist = new CalcDist(pX[p1], pY[p1], pX[p2], pY[p2], step);
                            dists[dist_i] = calcDist;
                        } else {
                            //長さを固定
                            int judgef = (dists[dist_i].reachfX >= dists[dist_i].reachfY) ? dists[dist_i].reachfX : dists[dist_i].reachfY;
                            if (judgef <= framec) {
                                sumpX2 = pX[p2];
                                sumpY2 = pY[p2];
                                if (j >= step && step >= l / 2 - 1 && forward) {
                                    state = false;
                                }
                            } else {
                                //フレーム毎に線を伸ばす
                                sumpX2 = pX[p1] + dists[dist_i].delX * (framec - (j / 2) * interval);
                                sumpY2 = pY[p1] + dists[dist_i].delY * (framec - (j / 2) * interval);
                            }
                        }

                        //動作反転時に始点で止める処理
                        if (framec < (j/2) * interval) {
                            sumpX2 = pX[p1];
                            sumpY2 = pY[p1];
                        }
                    }

                    //線を描画
                    p.setColor(randomColor);
                    c.drawLine((float) pX[p1], (float) pY[p1], (float) sumpX2, (float) sumpY2, p);
                }
            }
            if (framec <= 0 && !forward) state = false;
            if (framec > 0 && framec % interval == 0 && step < l / 2 - 1) step += 2;

            p.setTextSize(24);
            String frameStr = "frame:" + String.format("%6d", framec);
            c.drawText("" + frameStr + ",   l: " + l, 5, 30, p);
        }

        class CalcDist {
            double dX = 0.0, dY = 0.0;
            double delX = 0.0, delY = 0.0;
            double dist = 0.0;
            int reachfX = 0;
            int reachfY = 0;

            public CalcDist(double pX1, double pY1, double pX2, double pY2, int step) {
                dX = pX2 - pX1;
                dY = pY2 - pY1;
                dist = Math.sqrt(dX * dX + dY * dY);
                delX = (dX / dist) * speed;
                delY = (dY / dist) * speed;

                double reachfX_d = Math.sqrt((dX / delX) * (dX / delX));
                double reachfY_d = Math.sqrt((dY / delY) * (dY / delY));

                reachfX = interval * (step / 2) + (int) (reachfX_d);
                reachfY = interval * (step / 2) + (int) (reachfY_d);
            }
        }

        float downX = 0, downY = 0;

        public boolean onTouchEvent(MotionEvent event) {
            float upX = 0, upY = 0;
            float tsh = 80;
            switch (event.getAction()) {
                //タッチ
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    break;
                //スワイプ
                case MotionEvent.ACTION_MOVE:
                    float currentX = event.getX();
                    float currentY = event.getY();
                    break;
                //リリース
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    upX = event.getX();
                    upY = event.getY();
                    if (upX + tsh < downX || downX + tsh < upX || upY + tsh < downY || downY + tsh < upY) {
                        forward = !forward;
                        if (!state) {
                            state = true;
                        }
                    } else {
                        MainActivity.this.finish();
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                    }

                    break;
            }
            return true;
        }
    }
}
