/**
 * CampusGigLogo.kt — Official Brand Logo Package
 *
 * Location: com.abpvt.campusgig_frontend.ui.logo.CampusGigLogo.kt
 *
 * Displays crops prepared directly from the supplied official JPEG.
 * The logo_full, logo_symbol, and logo_wordmark resources are the single
 * source of truth for all logo rendering — no redrawing or reinterpretation.
 */
package com.abpvt.campusgig_frontend.ui.logo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abpvt.campusgig_frontend.R

/**
 * CampusGigLogoIcon — Displays the cleanly extracted infinity-people symbol mark (transparent bg).
 *
 * @param size The width/height of the symbol icon.
 */
@Composable
fun CampusGigLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    onDarkBackground: Boolean? = null
) {
    val useDarkAsset = onDarkBackground
        ?: (MaterialTheme.colorScheme.background.luminance() < 0.45f)
    Image(
        painter = painterResource(
            id = if (useDarkAsset) R.drawable.logo_symbol_dark else R.drawable.logo_symbol
        ),
        contentDescription = "Campus Vault symbol",
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

/**
 * CampusGigHeaderLogo — Official wordmark crop from the supplied artwork.
 * No Compose text is used, so the letterforms and proportions never diverge.
 */
@Composable
fun CampusGigHeaderLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 28.dp,
    fontSize: Float = 20f,
    onDarkBackground: Boolean? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CampusGigLogoIcon(size = iconSize, onDarkBackground = onDarkBackground)
        Spacer(modifier = Modifier.width(8.dp))
        CampusGigWordmark(fontSize = fontSize, onDarkBackground = onDarkBackground)
    }
}

/**
 * CampusGigWordmark — The actual wordmark from the official artwork.
 */
@Composable
fun CampusGigWordmark(
    modifier: Modifier = Modifier,
    fontSize: Float = 20f,
    onDarkBackground: Boolean? = null
) {
    val useDarkAsset = onDarkBackground
        ?: (MaterialTheme.colorScheme.background.luminance() < 0.45f)
    Image(
        painter = painterResource(
            id = if (useDarkAsset) R.drawable.logo_wordmark_dark else R.drawable.logo_wordmark
        ),
        contentDescription = "Campus Vault",
        modifier = modifier.width((fontSize * 6.5f).dp),
        contentScale = ContentScale.Fit
    )
}

/**
 * CampusGigWordmarkGraphic — Displays the extracted wordmark PNG image.
 */
@Composable
fun CampusGigWordmarkGraphic(
    modifier: Modifier = Modifier,
    width: Dp = 180.dp,
    onDarkBackground: Boolean? = null
) {
    val useDarkAsset = onDarkBackground
        ?: (MaterialTheme.colorScheme.background.luminance() < 0.45f)
    Image(
        painter = painterResource(
            id = if (useDarkAsset) R.drawable.logo_wordmark_dark else R.drawable.logo_wordmark
        ),
        contentDescription = "Campus Vault Wordmark",
        modifier = modifier.width(width),
        contentScale = ContentScale.FillWidth
    )
}

/**
 * CampusGigFullLogo — Displays the cleanly extracted full logo (symbol + wordmark + tagline).
 * Transparent background, perfectly cropped.
 *
 * @param width The desired width; height is auto-scaled to preserve proportions.
 */
@Composable
fun CampusGigFullLogo(
    modifier: Modifier = Modifier,
    width: Dp = 240.dp,
    onDarkBackground: Boolean? = null
) {
    val useDarkAsset = onDarkBackground
        ?: (MaterialTheme.colorScheme.background.luminance() < 0.45f)
    Image(
        painter = painterResource(
            id = if (useDarkAsset) R.drawable.logo_full_dark else R.drawable.logo_full
        ),
        contentDescription = "Campus Vault Logo",
        modifier = modifier.width(width),
        contentScale = ContentScale.FillWidth
    )
}

/** Backward compatibility alias — delegates to CampusGigLogoIcon. */
@Composable
fun VLockLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 30.dp
) = CampusGigLogoIcon(modifier = modifier, size = size)
