package org.jpn.geckour.drawdome.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.os.Handler
import android.preference.PreferenceManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import java.util.*

class MainActivity : AppCompatActivity() {
    private var sp: SharedPreferences? = null
    private val MIN_DEFAULT = 4
    private val MAX_DEFAULT = 80
    private var min = MIN_DEFAULT
    private var max = MAX_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (sp?.getInt("min_vertices", -1) ?: 0 < 0) {
            sp?.edit()?.putInt("min_vertices", MIN_DEFAULT)?.apply()
        }
        min = sp?.getInt("min_vertices", MIN_DEFAULT) ?: MIN_DEFAULT

        if (sp?.getInt("max_vertices", -1) ?: 0 < 0) {
            sp?.edit()?.putInt("max_vertices", MAX_DEFAULT)?.apply()
        }
        max = sp?.getInt("max_vertices", MAX_DEFAULT) ?: MIN_DEFAULT

        setContentView(MyView(this))
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_settings) {
            startActivityForResult(Intent(this, Pref::class.java), 0)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //他classへのintentの戻り値を受け付ける
    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        //Pref.javaからの戻り値の場合
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                if (Integer.parseInt(sp?.getString("min_vertices", "4")) < 4) {
                    sp?.edit()?.putInt("min_vertices", 4)?.apply()
                }
                if (Integer.parseInt(sp?.getString("max_vertices", "80")) > 100) {
                    sp?.edit()?.putInt("max_vertices", 100)?.apply()
                }
            }
        }
    }

    internal inner class MyView(context: Context) : View(context) {
        private var framec = 0

        private val black = Color.rgb(0, 0, 0)
        private val white = Color.rgb(255, 255, 255)
        private val bgColor = if (Math.random() > 0.5) black else white
        private val color = getRandomColor()

        private var l = (min + Math.random() * (max - min + 1)).toInt()
        private val speed = (2 + Math.random() * 4).toFloat()
        private var radius: Float = 0.toFloat()
        private var disp = windowManager.defaultDisplay
        private var displaySize = Point()
        private var offsetX: Float = 0.toFloat()
        private var offsetY: Float = 0.toFloat()
        private var cr = Math.PI * 2 / l
        private val pX = FloatArray(l)
        private val pY = FloatArray(l)
        private var calcInfos: Array<Array<CalcInfo>> = Array(l, {i -> Array(l / 4, {i -> CalcInfo()})})
        private var step = 0
        private var interval = if (45 / speed >= 10) (45 / speed).toInt() else 10
        private var isContinue = BooleanArray(l / 2 + 3)
        private var frameTillReachX = IntArray(l / 2 + 3)
        private var frameTillReachY = IntArray(l / 2 + 3)

        private var p = Paint()

        private var state = true
        private var forward = true

        private val h = Handler()

        init {
            disp.getSize(displaySize)
            offsetX = (displaySize.x / 2).toFloat()
            offsetY = (displaySize.y / 2 - 50).toFloat()
            radius = if (offsetX < offsetY) offsetX * 0.9f else offsetY * 0.9f

            for (i in 0..l - 1) {
                pX[i] = Math.cos(cr * i).toFloat() * radius + offsetX
                pY[i] = Math.sin(cr * i).toFloat() * radius + offsetY
                Log.d("echo","pX[${String.format("%03d", i)}]:${String.format("%5f", pX[i])}\tpY[${String.format("%03d", i)}]:${String.format("%5f", pX[i])}")
            }
            for (i in 0..l / 2 - 1) {
                isContinue[i] = true
            }
            for (i in 0..l / 2 + 3 - 1) {
                frameTillReachX[i] = 0
                frameTillReachY[i] = 0
            }

            for (i in 0..l - 1) {
                for (j in 0..l / 4 - 1) {
                    val p1 = i
                    val p2 = (p1 + (j + 1) * 2) % l

                    calcInfos[p1][j].set(pX[p1], pY[p1], pX[p2], pY[p2], j)
                }
            }

            val timer = Timer(false)
            timer.schedule(object : TimerTask() {
                override fun run() {
                    h.post {
                        if (state) {
                            if (forward)
                                framec++
                            else
                                framec--
                            //再描画
                            invalidate()
                        }
                    }
                }
            }, 100, 24)

        }

        private fun getRandomColor(): Int {
            val limitL = 80
            val limitU = 170
            var r = (Math.random() * 256).toInt()
            var g = (Math.random() * 256).toInt()
            var b = (Math.random() * 256).toInt()

            while (r < limitL || g < limitL || b < limitL || limitU < r || limitU < g || limitU < b) {
                r = (Math.random() * 256).toInt()
                g = (Math.random() * 256).toInt()
                b = (Math.random() * 256).toInt()
            }

            return Color.argb(90, r, g, b)
        }

        public override fun onDraw(c: Canvas) {
            c.drawColor(bgColor)

            val calcInfosOnLastPoint = calcInfos[calcInfos.lastIndex]
            val frameEndX = calcInfosOnLastPoint[calcInfosOnLastPoint.lastIndex].frameTillReachX
            val frameEndY = calcInfosOnLastPoint[calcInfosOnLastPoint.lastIndex].frameTillReachY
            val frameEnd = Math.max(frameEndX, frameEndY)

            for (i in 0..l - 1) {
                for (j in 2..step step 2) {
                    val calcInfo = calcInfos[i][(j - 2) / 2]
                    //点1と点2を定義
                    val p1 = i
                    val p2 = (p1 + j) % l

                    //線の長さを定義、引き終わっていれば長さを固定
                    var sumpX2 = pX[p2]
                    var sumpY2 = pY[p2]
                    if (framec > Math.max(calcInfo.frameTillReachX, calcInfo.frameTillReachY)) {
                        if (framec > frameEnd) state = false
                    } else if (!(!forward && framec < j / 2 * interval)) { //動作反転時は始点で止める
                        //フレーム毎に線を伸ばす
                        sumpX2 = pX[p1] + (calcInfo.delX) * (framec - (j / 2) * interval)
                        sumpY2 = pY[p1] + (calcInfo.delY) * (framec - (j / 2) * interval)
                    }

                    //線を描画
                    p.color = color
                    c.drawLine(pX[p1], pY[p1], sumpX2, sumpY2, p)
                }
            }
            if (framec == 0 && !forward) state = false
            if (framec > 0 && framec % interval == 0 && step < l / 2 - 1) step += 2

            p.textSize = 24f
            c.drawText("frame: $framec, l: $l", 5f, 30f, p)
        }

        internal inner class CalcInfo(pX1: Float = 0f, pY1: Float = 0f, pX2: Float = 0f, pY2: Float = 0f, step: Int = -1) {
            var delX = 0f
            var delY = 0f
            var frameTillReachX = -1
            var frameTillReachY = -1
            var step = -1

            init {
                set(pX1, pY1, pX2, pY2, step)
            }

            fun set(pX1: Float, pY1: Float, pX2: Float, pY2: Float, step: Int) {
                this.step = step
                val dX = pX2 - pX1
                val dY = pY2 - pY1
                val dist = Math.sqrt(Math.pow(dX.toDouble(), 2.0) + Math.pow(dY.toDouble(), 2.0)).toFloat()
                delX = (dX / dist) * speed
                delY = (dY / dist) * speed
                Log.d("echo", "delX: $delX, delY: $delY")
                val frameRXd = Math.abs(dX / delX).toInt()
                val frameRYd = Math.abs(dY / delY).toInt()
                frameTillReachX = interval * (step + 1) + frameRXd
                frameTillReachY = interval * (step + 1) + frameRYd
            }
        }

        var downX = 0f
        var downY = 0f
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val tsh = 80f
            when (event.action) {
            //タッチ
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    downY = event.y
                }
            //スワイプ
                MotionEvent.ACTION_MOVE -> {
                    val currentX = event.x
                    val currentY = event.y
                }
            //リリース
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val upX = event.x
                    val upY = event.y
                    if (upX + tsh < downX || downX + tsh < upX || upY + tsh < downY || downY + tsh < upY) {
                        forward = !forward
                        if (!state) {
                            state = true
                        }
                    } else {
                        this@MainActivity.finish()
                        startActivity(Intent(this@MainActivity, MainActivity::class.java))
                    }
                }
            }
            return true
        }
    }
}
