package com.miai.offline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miai.offline.ui.theme.*

/**
 * Lightweight text-based brand mark used in app bars / headers, echoing the
 * uploaded logo's orange→pink→purple→blue gradient without needing image assets.
 */
@Composable
fun MiAiBrandMark(modifier: Modifier = Modifier, showStatus: Boolean = true) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MiBrandGradient, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MI",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Column {
            Text(
                text = "MI AI",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            if (showStatus) {
                Text(
                    text = "OFFLINE",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
