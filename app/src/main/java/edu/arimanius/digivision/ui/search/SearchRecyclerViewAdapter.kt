package edu.arimanius.digivision.ui.search

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import edu.arimanius.digivision.api.search.Product

import edu.arimanius.digivision.databinding.FragmentProductItemBinding

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
    }

    fun updateProducts(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = products.size

    inner class ViewHolder(binding: FragmentProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val titleView: TextView = binding.productTitle

        override fun toString(): String {
            return super.toString() + " '" + titleView.text + "'"
        }
    }

}