package edu.arimanius.digivision.ui.product

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import edu.arimanius.digivision.api.search.Product
import edu.arimanius.digivision.api.search.category
import edu.arimanius.digivision.api.search.product
import edu.arimanius.digivision.api.search.rating
import edu.arimanius.digivision.databinding.FragmentProductBinding
import edu.arimanius.digivision.ui.breadcrumb.Breadcrumb
import edu.arimanius.digivision.ui.breadcrumb.BreadcrumbView


class ProductFragment : Fragment() {
    private var _product: Product? = null
    private val product get() = _product!!
    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            _product = product {
                id = it.getInt("product_id")
                title = it.getString("product_title")!!
                url = it.getString("product_url")!!
                imageUrl = it.getString("product_imageUrl")!!
                status = it.getString("product_status")!!
                price = it.getLong("product_price")
                rate = rating {
                    rate = it.getInt("product_rate_rate")
                    count = it.getInt("product_rate_count")
                }
                categories.addAll(
                    it.getStringArrayList("category_titles")!!
                        .zip(it.getStringArrayList("category_urls")!!) { title, url ->
                    category {
                        this.title = title
                        this.url = url
                    }
                })
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(binding.root).load(product.imageUrl).into(binding.imageView)
        binding.productTitle.text = product.title
        binding.price.text = if (product.status == "marketable") {
            "${product.price / 10} تومان"
        } else {
            "ناموجود"
        }
        binding.ratingBar.rating = product.rate.rate.toFloat() / 20
        binding.ratingCount.text = "${product.rate.count} رای"
        binding.productTitle.setOnClickListener {
            val uri = Uri.parse(product.url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        binding.breadCrumbView.setBreadcrumbList(product.categoriesList.map {
            Breadcrumb.Builder().setTitle(it.title).setTag(it.url).build()
        }.toMutableList())

        binding.breadCrumbView.setListener(object : BreadcrumbView.Listener {
            override fun onBreadcrumbSelected(crumb: Breadcrumb) {
                val uri = Uri.parse(crumb.getTag() as String)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        })
    }
}