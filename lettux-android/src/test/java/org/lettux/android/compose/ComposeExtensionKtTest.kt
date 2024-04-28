package org.lettux.android.compose

import android.app.Application
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.lettux.core.State
import org.lettux.factory.storeFactory
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ComposeExtensionKtTest {

    @get:Rule(order = 1)
    val addActivityToRobolectricRule = object : TestWatcher() {
        override fun starting(description: Description?) {
            super.starting(description)
            val appContext: Application = ApplicationProvider.getApplicationContext()
            val activityInfo = ActivityInfo().apply {
                name = ComponentActivity::class.java.name
                packageName = appContext.packageName
            }
            shadowOf(appContext.packageManager).addOrUpdateActivity(activityInfo)
        }
    }

    @get:Rule(order = 2)
    val composeTestRule = createComposeRule()

    data class PlainState(val value: Int = 0) : State

    @Test
    fun `currentStore should retrieve the store of the expected state`() = runTest {
        val store = storeFactory(
            initialState = PlainState(),
            actionHandler = { },
        ).get(this)

        composeTestRule.setContent {
            WithStore(store = store) {
                val current = currentStore<PlainState>()

                current shouldBe store
            }
        }
    }

    @Test
    fun `currentStore should throw an error when the store is not found`() = runTest {
        val exception = assertThrows<IllegalStateException> {
            composeTestRule.setContent {
                currentStore<PlainState>()
            }
        }
        exception.message shouldBe """
            No store of type ${PlainState::class.qualifiedName} found. Available stores: ${emptyList<String>()}
        """.trimIndent()
    }
}
