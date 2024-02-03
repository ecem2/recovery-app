package com.adentech.recovery.ui.result.videos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adentech.recovery.core.adapters.RecoveryBaseViewHolder
import com.adentech.recovery.data.model.FileModel
import com.adentech.recovery.databinding.ItemDeletedVideoBinding
import com.adentech.recovery.extensions.executeAfter

class DeletedVideoViewHolder(
    parent: ViewGroup, inflater: LayoutInflater
) : RecoveryBaseViewHolder<ItemDeletedVideoBinding>(
    binding = ItemDeletedVideoBinding.inflate(inflater, parent, false)
) {

    fun bind(item: FileModel, onItemClicked: ((item: FileModel) -> Unit)? = null) {
        binding.executeAfter {
            this.item = item
            tvVideoName.text = item.fileName
            videoSizeTV.text = item.fileExtension
            root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClicked?.invoke(item)
                }
            }
        }
    }
}