package dev.foss.goldenpath

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class GoldenPathUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun opensSettingsPanelWithThemeAndUpdateControls() {
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Check for updates").assertIsDisplayed()
        composeTestRule.onNodeWithText("Include system apps").assertIsDisplayed()
        composeTestRule.onNodeWithText("On demand").assertIsDisplayed()
        composeTestRule.onNodeWithText("Once a week").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark theme").performClick()
        composeTestRule.onNodeWithText("Close settings").performScrollTo().performClick()
    }

    @Test
    fun systemBackFromSettingsReturnsToMain() {
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Close settings").performScrollTo().assertIsDisplayed()
        composeTestRule.runOnIdle {
            composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.onNodeWithText("Close settings").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Settings").assertIsDisplayed()
    }

    @Test
    fun opensAboutPanelWithVersion() {
        composeTestRule.onNodeWithContentDescription("About").performClick()
        composeTestRule.onNodeWithText("About").assertIsDisplayed()
        composeTestRule.onNodeWithText("Installed format: apk").assertIsDisplayed()
    }
}
