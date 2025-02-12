package com.drewel.drewel.utill

import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

class EqualSpacingItemDecoration  (private val spacing: Int, private val displayMode: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildViewHolder(view).adapterPosition
        val itemCount = state!!.itemCount
        val layoutManager = parent.layoutManager
        setSpacingForDirection(outRect, layoutManager!!, position, itemCount)
    }



    private fun setSpacingForDirection(outRect: Rect,
                                       layoutManager: RecyclerView.LayoutManager,
                                       position: Int,
                                       itemCount: Int) {

        when (displayMode) {
            HORIZONTAL -> {
                outRect.left = spacing
                outRect.right = if (position == itemCount - 1) spacing else 0
                outRect.top = spacing
                outRect.bottom = spacing
            }
            VERTICAL -> {
                outRect.left = spacing
                outRect.right = spacing
                outRect.top = spacing
                outRect.bottom = if (position == itemCount - 1) spacing else 0
            }
            GRID -> if (layoutManager is GridLayoutManager) {
                val cols = layoutManager.spanCount
                val rows = itemCount / cols

                outRect.left = spacing
                outRect.right = if (position % cols == cols - 1) spacing else 0
                outRect.top = spacing
                outRect.bottom = if (position / cols == rows - 1) spacing else 0
            }
        }
    }

    companion object {

        const val HORIZONTAL = 0
        const val VERTICAL = 1
        const val GRID = 2
    }
}