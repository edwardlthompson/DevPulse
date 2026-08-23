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
        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Appearance").performClick()
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark theme").performScrollTo().performClick()
        composeTestRule.onNodeWithContentDescription("Back to settings").performClick()
        composeTestRule.onNodeWithText("Updates").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Check for updates").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back to settings").performClick()
        composeTestRule.onNodeWithText("History").performScrollTo().performClick()
        composeTestRule.onNodeWithText("History").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back to settings").performClick()
        composeTestRule.onNodeWithText("Inventory").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Include system apps").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("On demand").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Once a week").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back to settings").performClick()
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
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("About").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Installed format: apk").assertIsDisplayed()
    }
}
