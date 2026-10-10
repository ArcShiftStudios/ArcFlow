package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalArcTheme
import com.example.viewmodel.NavigationDestination

@Composable
fun FloatingBottomNavIsland(
    currentDestination: NavigationDestination,
    onNavigate: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalArcTheme.current
    val colorScheme = MaterialTheme.colorScheme
    val isDark = theme.isDark
    val primaryColor = theme.preset.primary

    val islandBg = if (isDark) {
        Color(0xEE12141F)
    } else {
        Color(0xEEFFFFFF)
    }

    val islandBorder = if (isDark) {
        Color(0x3338BDF8)
    } else {
        Color(0x220F172A)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = primaryColor.copy(alpha = 0.25f),
                    ambientColor = colorScheme.onSurface.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(islandBg)
                .border(1.dp, islandBorder, RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                destination = NavigationDestination.TODAY,
                isSelected = currentDestination == NavigationDestination.TODAY,
                label = "Today",
                selectedIcon = Icons.Filled.WbSunny,
                unselectedIcon = Icons.Outlined.WbSunny,
                onClick = { onNavigate(NavigationDestination.TODAY) }
            )
            NavItem(
                destination = NavigationDestination.CALENDAR,
                isSelected = currentDestination == NavigationDestination.CALENDAR,
                label = "Calendar",
                selectedIcon = Icons.Filled.CalendarMonth,
                unselectedIcon = Icons.Outlined.CalendarMonth,
                onClick = { onNavigate(NavigationDestination.CALENDAR) }
            )
            NavItem(
                destination = NavigationDestination.TASKS,
                isSelected = currentDestination == NavigationDestination.TASKS,
                label = "Tasks",
                selectedIcon = Icons.Filled.CheckCircle,
                unselectedIcon = Icons.Outlined.CheckCircle,
                onClick = { onNavigate(NavigationDestination.TASKS) }
            )
            NavItem(
                destination = NavigationDestination.STATS,
                isSelected = currentDestination == NavigationDestination.STATS,
                label = "Stats",
                selectedIcon = Icons.Filled.Insights,
                unselectedIcon = Icons.Outlined.Insights,
                onClick = { onNavigate(NavigationDestination.STATS) }
            )
            NavItem(
                destination = NavigationDestination.SETTINGS,
                isSelected = currentDestination == NavigationDestination.SETTINGS,
                label = "Settings",
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                onClick = { onNavigate(NavigationDestination.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavItem(
    destination: NavigationDestination,
    isSelected: Boolean,
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit
) {
    val theme = LocalArcTheme.current
    val colorScheme = MaterialTheme.colorScheme
    val isDark = theme.isDark
    val primaryColor = theme.preset.primary

    val interactionSource = remember { MutableInteractionSource() }

    val bgModifier = if (isSelected) {
        Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        primaryColor.copy(alpha = if (isDark) 0.35f else 0.22f),
                        theme.preset.secondary.copy(alpha = if (isDark) 0.25f else 0.15f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = primaryColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            )
    } else {
        Modifier.clip(CircleShape)
    }

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) primaryColor else colorScheme.onSurfaceVariant,
        label = "icon_color"
    )

    Row(
        modifier = Modifier
            .then(bgModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 24.dp),
                onClick = onClick
            )
            .padding(horizontal = if (isSelected) 14.dp else 10.dp, vertical = 8.dp)
            .testTag("nav_item_${destination.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 12.sp
            )
        }
    }
}
