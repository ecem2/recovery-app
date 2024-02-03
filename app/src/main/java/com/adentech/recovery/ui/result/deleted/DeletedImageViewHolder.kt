package com.adentech.recovery.ui.result.deleted

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adentech.recovery.core.adapters.RecoveryBaseViewHolder
import com.adentech.recovery.data.model.FileModel
import com.adentech.recovery.databinding.ItemDeletedImageBinding
import com.adentech.recovery.extensions.executeAfter

class DeletedImageViewHolder(
    parent: ViewGroup, inflater: LayoutInflater
) : RecoveryBaseViewHolder<ItemDeletedImageBinding>(
    binding = ItemDeletedImageBinding.inflate(inflater, parent, false)
) {
    fun bind(item: FileModel, onItemClicked: ((item: FileModel) -> Unit)? = null) {
        binding.executeAfter {
            this.item = item
            root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClicked?.invoke(item)
                }
            }
        }
    }
}