package com.abpvt.campusgig_frontend.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abpvt.campusgig_frontend.ui.logo.CampusGigLogoIcon

/**
 * Legacy compatibility alias for CampusGigLogoIcon.
 * Primary Logo file is located in dedicated package:
 * com.abpvt.campusgig_frontend.ui.logo.CampusGigLogo.kt
 */
@Composable
fun CampusGigLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 30.dp
) = CampusGigLogoIcon(modifier = modifier, size = size)

@Composable
fun VLockLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 30.dp
) = CampusGigLogoIcon(modifier = modifier, size = size)
