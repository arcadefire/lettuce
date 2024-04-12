@file:JvmName("StoreViewModelExtensions")

package org.lettux.android.extension

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.lettux.android.StoreViewModel
import org.lettux.core.State

@Composable
fun <STATE : State> StoreViewModel<STATE>.collectStatesWithLifecycle() = this.states.collectAsStateWithLifecycle()
