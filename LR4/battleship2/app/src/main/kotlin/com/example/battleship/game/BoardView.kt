package com.example.battleship.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.battleship.R
import com.example.battleship.common.BoardConstants
import com.example.battleship.common.CellState
import kotlin.math.min

class BoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, def: Int = 0
) : View(context, attrs, def) {

    interface OnCellClickListener { fun onCellClick(row: Int, col: Int) }
    var cellClickListener: OnCellClickListener? = null
    var isInteractive = false
    var showShips     = true

    private val size = BoardConstants.SIZE
    private var cellSize = 0f
    private val data = Array(size) { Array(size) { CellState.EMPTY } }

    private fun mkPaint(colorRes: Int, style: Paint.Style = Paint.Style.FILL) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color      = ContextCompat.getColor(context, colorRes)
            this.style = style
        }

    private val bgPaint    = mkPaint(R.color.board_bg)
    private val gridPaint  = mkPaint(R.color.board_grid, Paint.Style.STROKE).apply { strokeWidth = 1.2f }
    private val shipPaint  = mkPaint(R.color.ship_color)
    private val hitPaint   = mkPaint(R.color.hit_color)
    private val missPaint  = mkPaint(R.color.miss_color)
    private val sunkPaint  = mkPaint(R.color.sunk_color)
    private val crossPaint = mkPaint(R.color.hit_cross, Paint.Style.STROKE).apply {
        strokeWidth = 3f; strokeCap = Paint.Cap.ROUND
    }
    private val dotPaint   = mkPaint(R.color.miss_dot)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x33FFFFFF; style = Paint.Style.FILL
    }

    // Preview cell for placement
    private var previewRow = -1
    private var previewCol = -1

    override fun onMeasure(ws: Int, hs: Int) {
        val d = min(MeasureSpec.getSize(ws), MeasureSpec.getSize(hs))
        setMeasuredDimension(d, d)
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        cellSize = min(w, h).toFloat() / size
    }

    override fun onDraw(canvas: Canvas) {
        // Fill background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        for (r in 0 until size) {
            for (c in 0 until size) {
                val left   = c * cellSize + 1f
                val top    = r * cellSize + 1f
                val right  = (c + 1) * cellSize - 1f
                val bottom = (r + 1) * cellSize - 1f
                val rect   = RectF(left, top, right, bottom)

                when (data[r][c]) {
                    CellState.SHIP -> if (showShips) {
                        canvas.drawRoundRect(rect, 4f, 4f, shipPaint)
                    }
                    CellState.HIT -> {
                        canvas.drawRoundRect(rect, 4f, 4f, hitPaint)
                        drawCross(canvas, rect)
                    }
                    CellState.SUNK -> {
                        canvas.drawRoundRect(rect, 4f, 4f, sunkPaint)
                        drawCross(canvas, rect)
                    }
                    CellState.MISS -> {
                        canvas.drawRoundRect(rect, 4f, 4f, missPaint)
                        val cx = rect.centerX(); val cy = rect.centerY()
                        canvas.drawCircle(cx, cy, cellSize * 0.16f, dotPaint)
                    }
                    CellState.EMPTY -> {}
                }

                // Highlight preview cell
                if (r == previewRow && c == previewCol && isInteractive) {
                    canvas.drawRoundRect(rect, 4f, 4f, highlightPaint)
                }

                canvas.drawRect(rect, gridPaint)
            }
        }
    }

    private fun drawCross(canvas: Canvas, r: RectF) {
        val p = r.width() * 0.2f
        canvas.drawLine(r.left + p, r.top + p, r.right - p, r.bottom - p, crossPaint)
        canvas.drawLine(r.right - p, r.top + p, r.left + p, r.bottom - p, crossPaint)
    }

    fun updateBoard(board: List<Int>) {
        if (board.size != 100) return
        board.forEachIndexed { i, v ->
            data[BoardConstants.row(i)][BoardConstants.col(i)] =
                CellState.values().getOrElse(v) { CellState.EMPTY }
        }
        invalidate()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (!isInteractive) return false
        val col = (e.x / cellSize).toInt().coerceIn(0, size - 1)
        val row = (e.y / cellSize).toInt().coerceIn(0, size - 1)

        when (e.action) {
            MotionEvent.ACTION_MOVE -> {
                previewRow = row; previewCol = col; invalidate()
            }
            MotionEvent.ACTION_UP -> {
                previewRow = -1; previewCol = -1; invalidate()
                val st = data[row][col]
                if (st == CellState.EMPTY || st == CellState.SHIP) {
                    cellClickListener?.onCellClick(row, col)
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                previewRow = -1; previewCol = -1; invalidate()
            }
        }
        return true
    }
}
