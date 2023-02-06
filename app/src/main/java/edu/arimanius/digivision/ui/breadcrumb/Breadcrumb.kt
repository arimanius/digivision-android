package edu.arimanius.digivision.ui.breadcrumb

class Breadcrumb {
    private var title: String? = null
    private var tag: Any? = null

    fun getTitle() = title

    fun getTag() = tag

    class Builder() {

        private val crumb: Breadcrumb = Breadcrumb()

        fun setTitle(title: String): Builder {
            crumb.title = title
            return this
        }

        fun setTag(tag: Any): Builder {
            crumb.tag = tag
            return this
        }

        fun build(): Breadcrumb {
            return crumb
        }

    }
}