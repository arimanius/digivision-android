package edu.arimanius.digivision.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.arimanius.digivision.R
import edu.arimanius.digivision.api.search.Product
import edu.arimanius.digivision.databinding.FragmentProductItemBinding
import edu.arimanius.digivision.ui.product.*

class SearchRecyclerViewAdapter(
    private var products: List<Product> = emptyList(),
    private var isLoading: Boolean = true,
) : RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentProductItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == products.size) {
            if (isLoading) {
                holder.progressBar.visibility = View.VISIBLE
                holder.spacer.visibility = View.GONE
                holder.divider.visibility = View.GONE
            } else {
                holder.progressBar.visibility = View.GONE
                holder.spacer.visibility = View.VISIBLE
            }
            holder.divider.visibility = View.GONE
            holder.titleView.visibility = View.GONE
            holder.priceView.visibility = View.GONE
            holder.imageView.visibility = View.GONE
        } else {
            holder.divider.visibility = View.VISIBLE
            holder.progressBar.visibility = View.GONE
            holder.spacer.visibility = View.GONE
            holder.titleView.visibility = View.VISIBLE
            holder.priceView.visibility = View.VISIBLE
            holder.imageView.visibility = View.VISIBLE
            val item = products[position]
            holder.titleView.text = item.title
            holder.priceView.text = if (item.status == "marketable") {
                "${item.price / 10} تومان"
            } else {
                "ناموجود"
            }
            holder.binding.root.setOnClickListener {
                val bundle = Bundle()
                Log.d("a", item.toString())
                bundle.putInt("product_id", item.id)
                bundle.putString("product_title", item.title)
                bundle.putString("product_url", item.url)
                bundle.putString("product_imageUrl", item.imageUrl)
                bundle.putString("product_status", item.status)
                bundle.putLong("product_price", item.price)
                bundle.putInt("product_rate_rate", item.rate.rate)
                bundle.putInt("product_rate_count", item.rate.count)
                bundle.putStringArrayList(
                    "category_titles",
                    ArrayList(item.categoriesList.map { it.title })
                )
                bundle.putStringArrayList(
                    "category_urls",
                    ArrayList((item.categoriesList.map { it.url }))
                )
                holder.binding.root.findNavController()
                    .navigate(R.id.action_searchFragment_to_productFragment, bundle)
            }
            Glide.with(holder.binding.root).load(item.imageUrl).into(holder.imageView)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateProducts(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }

    fun updateLoading(isLoading: Boolean) {
        this.isLoading = isLoading
        notifyItemChanged(products.size)
    }

    fun clearProducts() {
        val lastLength = this.products.size
        this.products = emptyList()
        notifyItemRangeRemoved(0, lastLength)
    }

    override fun getItemCount(): Int = products.size + 1

    inner class ViewHolder(val binding: FragmentProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val titleView: TextView = binding.productTitle
        val priceView: TextView = binding.price
        val imageView: ImageView = binding.imageView
        val progressBar: ProgressBar = binding.progressBar
        val spacer: View = binding.spacer
        val divider: View = binding.divider

        override fun toString(): String {
            return super.toString() + " '" + titleView.text + "'"
        }
    }

}