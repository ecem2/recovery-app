package com.adentech.recovery.ui.scan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adentech.recovery.core.adapters.RecoveryBaseViewHolder
import com.adentech.recovery.data.model.FileModel
import com.adentech.recovery.databinding.ItemFreeScanImageBinding
import com.adentech.recovery.extensions.executeAfter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import jp.wasabeef.glide.transformations.BlurTransformation

class FreeScanViewHolder(
    parent: ViewGroup, inflater: LayoutInflater
) : RecoveryBaseViewHolder<ItemFreeScanImageBinding>(
    binding = ItemFreeScanImageBinding.inflate(inflater, parent, false)
) {

    fun bind(item: FileModel, onItemClicked: ((item: FileModel, position: Int) -> Unit)? = null) {
        binding.executeAfter {
            this.item = item
            root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClicked?.invoke(item, adapterPosition)
                }
            }
        }
    }
}