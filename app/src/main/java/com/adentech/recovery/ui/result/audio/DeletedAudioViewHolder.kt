package com.adentech.recovery.ui.result.audio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adentech.recovery.core.adapters.RecoveryBaseViewHolder
import com.adentech.recovery.data.model.FileModel
import com.adentech.recovery.databinding.ItemDeletedAudioBinding
import com.adentech.recovery.extensions.executeAfter

class DeletedAudioViewHolder(
    parent: ViewGroup, inflater: LayoutInflater
) : RecoveryBaseViewHolder<ItemDeletedAudioBinding>(
    binding = ItemDeletedAudioBinding.inflate(inflater, parent, false)
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