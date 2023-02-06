package edu.arimanius.digivision.ui.breadcrumb

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.arimanius.digivision.R

class BreadcrumbAdapter(private val listener: BreadcrumbView.AdapterClickListener) : RecyclerView.Adapter<BreadcrumbAdapter.ViewHolder>() {

    private var breadcrumbList: MutableList<Breadcrumb>? = null
    private var arrowDrawable: Int = R.drawable.ic_baseline_arrow_forward_ios_24
    private var textColor: Int = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.breadcrumb_item, parent, false), arrowDrawable, listener, textColor)
    }

    override fun getItemCount(): Int {
        return breadcrumbList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = breadcrumbList?.get(position)
        Log.d("BreadcrumbAdapter", "item: ${item?.getTitle()}")

        if (position == 0) {
            holder.image.visibility = View.GONE
        } else {
            holder.image.visibility = View.VISIBLE
        }

        holder.title.text = item?.getTitle()

    }

    fun setArrow(arrowDrawable: Int) {
        this.arrowDrawable = arrowDrawable
        notifyDataSetChanged()
    }

    fun setList(list: MutableList<Breadcrumb>) {
        breadcrumbList = list
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Breadcrumb? {
        return breadcrumbList?.get(position)
    }

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View, arrowDrawable: Int, private val listener: BreadcrumbView.AdapterClickListener, textColor: Int) : RecyclerView.ViewHolder(itemView) {

        var title: TextView = itemView.findViewById(R.id.crumb_title)
        var image: ImageView = itemView.findViewById(R.id.crumb_image)

        init {
            image.setImageResource(arrowDrawable)

            title.setOnClickListener {
                listener.onItemClick(it, bindingAdapterPosition)
            }

            if (textColor != -1)
                title.setTextColor(textColor)

        }
    }
}