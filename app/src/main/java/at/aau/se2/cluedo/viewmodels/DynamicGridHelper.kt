package at.aau.se2.cluedo.viewmodels

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Helper class to calculate grid dimensions based on screen size and number of items
 */
class DynamicGridHelper(private val context: Context) {

    /**
     * Calculate the optimal number of columns for a grid based on screen width
     * and desired minimum column width
     */
    fun calculateOptimalColumnCount(
        itemCount: Int,
        minColumnWidth: Int = 120, // minimum width in dp
        maxColumns: Int = 6 // maximum number of columns
    ): Int {
        // Convert min column width from dp to pixels
        val metrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.currentWindowMetrics

        val minColumnWidthPx = dpToPx(minColumnWidth.toFloat(), metrics)

        // Calculate max columns that can fit on screen
        val screenWidthPx = metrics.widthPixels
        val maxColumnsFromWidth = (screenWidthPx / minColumnWidthPx).toInt()

        // Determine column count based on constraints
        var columnCount = minOf(maxColumnsFromWidth, maxColumns)

        // Adjust based on number of items (if we have fewer items than max columns)
        if (itemCount > 0 && itemCount < columnCount) {
            columnCount = itemCount
        }

        // Enforce minimum of 2 columns, but don't exceed item count
        return if (itemCount <= 1) 1 else maxOf(2, columnCount)
    }

    /**
     * Calculate grid column count based on total number of items
     * Guarantees a balanced layout with 3-6 columns
     */
    fun getColumnCountForPlayerCards(itemCount: Int): Int {
        return when {
            itemCount <= 2 -> 2
            itemCount <= 3 -> 3
            itemCount <= 4 -> 2  // 2x2 grid is better for 4 items
            itemCount <= 6 -> 3
            itemCount <= 8 -> 4
            itemCount <= 9 -> 3  // 3x3 is better for 9 items
            itemCount <= 12 -> 4
            itemCount <= 15 -> 5
            else -> 6
        }
    }

    private fun dpToPx(dp: Float, metrics: DisplayMetrics): Float {
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}