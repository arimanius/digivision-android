package edu.arimanius.digivision.ui.search

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.arimanius.digivision.api.search.Product
import edu.arimanius.digivision.databinding.FragmentProductItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class SearchRecyclerViewAdapter(
    private var products: List<Product> = emptyList()
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
        val item = products[position]
        holder.titleView.text = item.title
        holder.priceView.text = if(item.status == "marketable") {
            "${item.price/10} تومان"
        } else {
            "ناموجود"
        }
        Glide.with(holder.binding.root).load(item.imageUrl).into(holder.imageView)
    }

    fun updateProducts(products: List<Product>) {
        this.products = products
        notifyItemInserted(products.size)
    }

    override fun getItemCount(): Int = products.size

    inner class ViewHolder(val binding: FragmentProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val titleView: TextView = binding.productTitle
        val priceView: TextView = binding.price
        val imageView: ImageView = binding.imageView

        override fun toString(): String {
            return super.toString() + " '" + titleView.text + "'"
        }
    }

}