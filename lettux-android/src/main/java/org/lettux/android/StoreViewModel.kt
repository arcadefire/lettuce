package org.lettux.android

import androidx.lifecycle.ViewModel
import org.lettux.core.Store

class StoreViewModel<STATE> constructor(
    private val store: Store<STATE>
) : ViewModel() {
}