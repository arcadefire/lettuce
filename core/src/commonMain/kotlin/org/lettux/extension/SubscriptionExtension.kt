package org.lettux.extension

import kotlinx.coroutines.flow.merge
import org.lettux.core.State
import org.lettux.core.Subscription

fun <STATE : State> combine(vararg subscriptions: Subscription<STATE>): Subscription<STATE> {
    return if (subscriptions.size == 1) {
        subscriptions.first()
    } else {
        Subscription { states ->
            subscriptions
                .drop(1)
                .fold(subscriptions.first().subscribe(states)) { acc, subscription ->
                    merge(acc, subscription.subscribe(states))
                }
        }
    }
}

operator fun <S : State> Subscription<S>.plus(another: Subscription<S>): Subscription<S> {
    return combine(this, another)
}
