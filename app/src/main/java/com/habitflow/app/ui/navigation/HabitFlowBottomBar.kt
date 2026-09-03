package com.habitflow.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.habitflow.app.core.designsystem.NotionTheme

@Composable
fun HabitFlowBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val colors = NotionTheme.colors
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = remember { Screen.bottomNavItems }
    val shouldShow = navItems.any { it.route == currentRoute }
    if (!shouldShow) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.25f to colors.background.copy(alpha = 0.85f),
                    0.45f to colors.background,
                    1.0f to colors.background
                )
            )
            .padding(top = 18.dp)
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating Island Dock: Fixed, Symmetrical & Protected
        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = colors.textPrimary.copy(alpha = 0.05f),
                    spotColor = colors.textPrimary.copy(alpha = 0.10f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surface)
                .border(1.dp, colors.border.copy(alpha = 0.65f), RoundedCornerShape(28.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    val isAddButton = screen == Screen.AddItem

                    if (isAddButton) {
                        // Central Tactile Add (+) Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    ambientColor = colors.accent.copy(alpha = 0.3f),
                                    spotColor = colors.accent.copy(alpha = 0.4f)
                                )
                                .clip(CircleShape)
                                .background(colors.accent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    navController.navigate(Screen.AddEditTimelineItem.createRoute())
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add New Intention",
                                tint = colors.onAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        // Symmetrical Tab Pill (Fixed footprint: never pushes adjacent items)
                        val iconScale by animateFloatAsState(
                            targetValue = if (selected) 1.15f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "tab_icon_scale"
                        )

                        val pillBg by animateColorAsState(
                            targetValue = if (selected) colors.accentSoft else Color.Transparent,
                            label = "tab_pill_bg"
                        )

                        val contentColor by animateColorAsState(
                            targetValue = if (selected) colors.accent else colors.textTertiary,
                            label = "tab_content_color"
                        )

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(pillBg)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                screen.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title,
                                        tint = contentColor,
                                        modifier = Modifier
                                            .size(21.dp)
                                            .scale(iconScale)
                                    )
                                }

                                if (selected) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(3.5.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
