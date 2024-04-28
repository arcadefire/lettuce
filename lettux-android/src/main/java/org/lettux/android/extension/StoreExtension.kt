@file:JvmName("StoreExtension")

package org.lettux.android.extension

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.lettux.core.State
import org.lettux.core.Store

@Composable
fun <STATE : State> Store<STATE>.collectStatesWithLifecycle() = this.states.collectAsStateWithLifecycle()
