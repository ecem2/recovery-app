package com.adentech.recovery.core.recyclerview

import com.adentech.recovery.core.adapters.RecoveryBaseListAdapter

abstract class RecoveryListAdapter<T : Any>(
    itemsSame: (oldItem: T, newItem: T) -> Boolean = { oldItem, newItem -> oldItem == newItem },
    contentsSame: (oldItem: T, newItem: T) -> Boolean = { oldItem, newItem -> oldItem == newItem }
) : RecoveryBaseListAdapter<T>(itemsSame, contentsSame)