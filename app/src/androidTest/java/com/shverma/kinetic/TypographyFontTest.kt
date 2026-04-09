package com.shverma.kinetic

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.shverma.kinetic.ui.theme.BarlowCondensedFamily
import com.shverma.kinetic.ui.theme.LexendFamily
import com.shverma.kinetic.ui.theme.PlusJakartaSansFamily
import com.shverma.kinetic.ui.theme.SpaceGroteskFamily
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class TypographyFontTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testDifferentFontFamilies() {
        // This test ensures that different font families are defined and can be used.
        // It helps verify that we are indeed using "diff fonts".
        
        val fonts = listOf(
            LexendFamily,
            PlusJakartaSansFamily,
            SpaceGroteskFamily,
            BarlowCondensedFamily
        )
        
        // Ensure they are distinct
        for (i in fonts.indices) {
            for (j in i + 1 until fonts.size) {
                assertNotEquals("Font families should be different", fonts[i], fonts[j])
            }
        }
    }
    
    @Test
    fun testTypographyStylesWithDifferentFonts() {
        val styles = listOf(
            TextStyle(fontFamily = LexendFamily),
            TextStyle(fontFamily = SpaceGroteskFamily),
            TextStyle(fontFamily = BarlowCondensedFamily),
            TextStyle(fontFamily = PlusJakartaSansFamily)
        )
        
        styles.forEach { style ->
            assertNotEquals(FontFamily.Default, style.fontFamily)
        }
    }
}
