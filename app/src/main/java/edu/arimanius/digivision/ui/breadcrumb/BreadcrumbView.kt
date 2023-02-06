package edu.arimanius.digivision.ui.breadcrumb

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.arimanius.digivision.R

class BreadcrumbView : FrameLayout {

    private var recyclerView: RecyclerView? = null
    private var breadcrumbClickListener: Listener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BreadcrumbView, defStyleAttr, 0)
            val arrowDrawable = typedArray.getResourceId(R.styleable.BreadcrumbView_arrow_drawable, -1)
            val textColor = typedArray.getColor(R.styleable.BreadcrumbView_bread_crumb_color, -1)
            typedArray.recycle()
            if (recyclerView?.adapter is BreadcrumbAdapter) {
                if (arrowDrawable != -1)
                    (recyclerView?.adapter as BreadcrumbAdapter).setArrow(arrowDrawable)
                if (textColor != -1)
                    (recyclerView?.adapter as BreadcrumbAdapter).setTextColor(textColor)
            }
        }
    }

    private fun init() {

        if (recyclerView == null) {

            val recyclerLayoutParams = ViewGroup.LayoutParams(-1, -1)
            recyclerLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

            recyclerView = RecyclerView(context)
            recyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            recyclerView?.adapter = BreadcrumbAdapter(object : AdapterClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    breadcrumbClickListener?.let {
                        if (recyclerView?.adapter is BreadcrumbAdapter) {
                            val item = (recyclerView?.adapter as BreadcrumbAdapter).getItem(position)
                            item?.let { crumb -> it.onBreadcrumbSelected(crumb) }
                        }
                    }
                }

            })

            addView(recyclerView, recyclerLayoutParams)
        }

    }

    fun setBreadcrumbList(list: MutableList<Breadcrumb>) {
        if (recyclerView?.adapter is BreadcrumbAdapter) {
            Log.d("BreadcrumbView", "${list.size}")
            (recyclerView?.adapter as BreadcrumbAdapter).setList(list)
        }

    }

    fun setListener(listener: Listener) {
        if (recyclerView?.adapter is BreadcrumbAdapter) {
            this.breadcrumbClickListener = listener
        }
    }

    interface Listener {
        fun onBreadcrumbSelected(crumb: Breadcrumb)
    }

    interface AdapterClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}