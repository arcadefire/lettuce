
# Lettux
Lettux is a lightweight, Redux-like library that provides a structured and efficient way to manage the state 
of your application and maintain it in good health.

## Core Concepts
At the heart of Lettux is the concept of a _Store_. 

It serves as a central repository for your application's state and provides a mechanism for 
interacting with and modifying it. 

The state held by the store can be reactively observed, 
allowing other parts of your application to subscribe to and react to its changes.

```kotlin
data class AppState(val counter: Int = 0)

val store = createStore<AppState>(
    initialState = AppState(), 
    actionHandler = counterActionHandler,
    storeScope = CoroutineScope(Dispatchers.Default),
)
```

## Actions
Changes to the state are triggered by _Actions_. _Actions_ are simple objects that represent an intention to modify 
the state. 

They don't contain any logic for how the state should be changed; instead, they act as triggers.

```kotlin
data class IncrementCounter(val amount: Int = 1) : Action

store.send(IncrementCounter(amount = 5))
```

## Action Handlers
_Action Handlers_ are responsible for processing actions and updating the state accordingly. 

They have access to the _Action Context_ that allows you to access the current state, commit a new state or 
dispatch a new action.

Ideally, each _Action Handler_ should encapsulate a specific piece of state management logic
(or a specific feature) making it easier to maintain.

```kotlin
val counterActionHandler = ActionHandler<AppState> { action: Action ->
    when (action) {
        is IncrementCounter -> {
            val newCounter = state.counter + action.amount
            commit(state.copy(counter = newCounter))
        }
        is DecrementCounter -> {
            val newCounter = state.counter - action.amount
            commit(state.copy(counter = min(newCounter, 0)))
        }
    }
}
```

## Subscriptions
Subscriptions provide a way to react to state changes. gcYou can tap into the flow of states and trigger new actions based on state transitions.

```kotlin
val counterSubscription = Subscription<AppState> { states: Flow<AppState> ->
    states.map {
        if (it.counter >= 10) {
            Decrement(amount = 10)
        } else {
            null
        }
    }.filterNotNull()
}
```

## Middlewares
Middlewares intercept actions before they reach the action handlers. 

They can be used for logging, state validation, or transforming actions, providing a powerful way 
to add cross-cutting concerns to your state management logic. 

For example, a middleware might log the state before and after an action is processed.
```kotlin
val loggerMiddleware = Middleware { action: Action, state: State, chain: Chain ->
    println("Action: $action")
    println("State before: $state")
    
    chain.proceed(action).also { outcome: Outcome ->
        if (outcome is Outcome.StateMutated) {
            println("State after: ${outcome.state}")
        }    
    }
} 
```

## State slicing
Lettux allows you to create slices of the central store's state.

These slices provide a focused view of a specific portion of the state, simplifying state management 
for complex applications by allowing different parts of your UI to work with only the state they need.

```kotlin
val slice = sliceStore(
    store = store,
    stateToSlice = { state: AppState -> state.counter },
    sliceToState = { state: AppState, slice: Int -> state.copy(counter = slice) }
)

slice.state.collect { counter: Int ->
    println("Counter: $counter")
}
```