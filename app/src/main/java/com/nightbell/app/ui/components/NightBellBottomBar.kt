package com.nightbell.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nightbell.app.ui.navigation.Screen
import com.nightbell.app.ui.theme.DarkBorder
import com.nightbell.app.ui.theme.DarkSurfaceNav
import com.nightbell.app.ui.theme.PrimaryAmber
import com.nightbell.app.ui.theme.TextMuted

@Composable
fun NightBellBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceNav)
    ) {
        HorizontalDivider(color = DarkBorder, thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .padding(top = 10.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                title = "Home",
                isSelected = currentScreen == Screen.Home,
                onClick = { onNavigate(Screen.Home) }
            )

            BottomBarItem(
                title = "Rules",
                isSelected = currentScreen == Screen.Rules,
                onClick = { onNavigate(Screen.Rules) }
            )

            BottomBarItem(
                title = "History",
                isSelected = currentScreen == Screen.History,
                onClick = { onNavigate(Screen.History) }
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) PrimaryAmber else TextMuted
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .size(width = 96.dp, height = 52.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
    ) {
        // Tab indicator icon
        when (title) {
            "Home" -> HomeIcon(color = color)
            "Rules" -> RulesIcon(color = color)
            else -> HistoryIcon(color = color)
        }

        Text(
            text = title,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun HomeIcon(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 0.16f, size.height * 0.45f)
            lineTo(size.width * 0.5f, size.height * 0.16f)
            lineTo(size.width * 0.84f, size.height * 0.45f)
            lineTo(size.width * 0.84f, size.height * 0.84f)
            lineTo(size.width * 0.16f, size.height * 0.84f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx())
        )
    }
}

@Composable
private fun RulesIcon(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = 1.8.dp.toPx()
        // Top line with circle
        drawLine(color, androidx.compose.ui.geometry.Offset(size.width * 0.16f, size.height * 0.3f), androidx.compose.ui.geometry.Offset(size.width * 0.58f, size.height * 0.3f), strokeWidth = stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(size.width * 0.75f, size.height * 0.3f), androidx.compose.ui.geometry.Offset(size.width * 0.84f, size.height * 0.3f), strokeWidth = stroke)
        drawCircle(color, radius = 2.5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * 0.66f, size.height * 0.3f), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))

        // Bottom line with circle
        drawLine(color, androidx.compose.ui.geometry.Offset(size.width * 0.16f, size.height * 0.7f), androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height * 0.7f), strokeWidth = stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 0.7f), androidx.compose.ui.geometry.Offset(size.width * 0.84f, size.height * 0.7f), strokeWidth = stroke)
        drawCircle(color, radius = 2.5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * 0.33f, size.height * 0.7f), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
    }
}

@Composable
private fun HistoryIcon(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = 1.8.dp.toPx()
        drawCircle(
            color = color,
            radius = size.width * 0.35f,
            center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.5f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.5f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.32f),
            strokeWidth = stroke
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.5f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.65f, size.height * 0.58f),
            strokeWidth = stroke
        )
    }
}
