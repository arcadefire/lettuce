package org.lettux.android.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import org.lettux.core.State
import org.lettux.core.Store
import kotlin.reflect.KClass

val localStoreCompositionMap =
    mutableMapOf<KClass<*>, ProvidableCompositionLocal<Store<*>>>()

@Composable
inline fun <reified S : State> WithStore(
    store: Store<S>,
    crossinline content: @Composable () -> Unit
) {
    val compositionProvider = localStoreCompositionMap.getOrPut(S::class) {
        compositionLocalOf {
            error("Missing store of type ${S::class.qualifiedName}")
        }
    }

    CompositionLocalProvider(compositionProvider provides store) {
        content()
    }
}

@Composable
inline fun <reified S : State> currentStore(): Store<S> {
    val store = localStoreCompositionMap[S::class]?.current as? Store<S>
        ?: error("No store of type ${S::class.qualifiedName} found. " +
                "Available stores: ${localStoreCompositionMap.keys}")
    return remember { store }
}
