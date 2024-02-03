package com.adentech.recovery.ui.scan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.adentech.recovery.R
import com.adentech.recovery.core.recyclerview.RecoveryListAdapter
import com.adentech.recovery.data.model.FileModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation

class FreeScanAdapter(
    var hasReward: Boolean = false,
    var isSubsClosed: Boolean = false,
    private val context: Context,
    private val onItemClicked: ((item: FileModel, position: Int) -> Unit)? = null
) : RecoveryListAdapter<FileModel>(
    itemsSame = { old, new -> old == new },
    contentsSame = { old, new -> old == new }
) {

    var rewardedList: List<Int>? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        inflater: LayoutInflater,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return FreeScanViewHolder(parent, inflater)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FreeScanViewHolder) {
            val item = getItem(position)
            holder.bind(item, onItemClicked)

            if (isSubsClosed) {
                holder.binding.ivReward.visibility = View.VISIBLE
                if (hasReward) {
                    if (rewardedList?.contains(position) == true) {
                        item.isRewarded = true
                        Glide.with(context).load(ContextCompat.getDrawable(context, R.mipmap.ic_ads))
                            .into(holder.binding.ivReward)
                    } else {
                        item.isRewarded = false
                        Glide.with(context).load(ContextCompat.getDrawable(context, R.mipmap.ic_crown))
                            .into(holder.binding.ivReward)
                    }
                } else {
                    item.isRewarded = false
                    Glide.with(context).load(ContextCompat.getDrawable(context, R.mipmap.ic_crown))
                        .into(holder.binding.ivReward)
                }
            }

            if (item.isSelected == true) {
                holder.binding.ivReward.visibility = View.GONE
                Glide.with(context).load(item.imageUri)
                    .into(holder.binding.ivRedBlurryImage)
            } else {
                Glide.with(context).load(item.imageUri)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(15, 3)))
                    .into(holder.binding.ivRedBlurryImage)
            }
        }
    }
}