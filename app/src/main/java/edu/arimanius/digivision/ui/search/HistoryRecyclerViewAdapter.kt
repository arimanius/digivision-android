package edu.arimanius.digivision.ui.search

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import edu.arimanius.digivision.MainActivity
import edu.arimanius.digivision.R
import edu.arimanius.digivision.data.entity.History
import edu.arimanius.digivision.databinding.FragmentHistoryItemBinding

class HistoryRecyclerViewAdapter(
    private var histories: List<History> = emptyList()
) : RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentHistoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = histories[position]
        holder.imageView.setImageURI(Uri.parse(item.imageUri))
        holder.imageView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("imageUri", item.imageUri)
            bundle.putLong("historyId", item.id)
            val fragment = SearchFragment()
            fragment.arguments = bundle
            (holder.itemView.context as MainActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, fragment).commit()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateHistories(histories: List<History>) {
        this.histories = histories
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = histories.size

    inner class ViewHolder(binding: FragmentHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imageView: ImageView = binding.historyImage
    }

}