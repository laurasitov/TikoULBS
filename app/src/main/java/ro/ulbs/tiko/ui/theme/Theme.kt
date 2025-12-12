package ro.ulbs.tiko.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    primaryContainer = GreenContainer,
    onPrimaryContainer = OnGreenContainer,
    secondary = BluePrimary,
    secondaryContainer = BlueContainer,
    onSecondaryContainer = OnBlueContainer,
    background = LightGray,
    surface = LightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    primaryContainer = GreenContainer,
    onPrimaryContainer = OnGreenContainer,
    secondary = BluePrimary,
    secondaryContainer = BlueContainer,
    onSecondaryContainer = OnBlueContainer,
    background = DarkGray,
    surface = DarkGray
)

@Composable
fun TikoULBSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
