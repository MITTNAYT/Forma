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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val shouldShow = Screen.bottomNavItems.any { it.route == currentRoute }
    if (!shouldShow) return

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        val isNarrow = maxWidth < 360.dp

        // Floating Island Dock for 5 Navigation Elements
        Box(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = colors.textPrimary.copy(alpha = 0.08f),
                    spotColor = colors.textPrimary.copy(alpha = 0.12f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(colors.surface)
                .border(1.dp, colors.border.copy(alpha = 0.65f), RoundedCornerShape(32.dp))
                .padding(horizontal = if (isNarrow) 4.dp else 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    val isAddButton = screen == Screen.AddItem

                    if (isAddButton) {
                        // Dedicated Central Add (+) Action Orb
                        Box(
                            modifier = Modifier
                                .size(if (isNarrow) 38.dp else 42.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    ambientColor = colors.accent.copy(alpha = 0.25f),
                                    spotColor = colors.accent.copy(alpha = 0.35f)
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
                                modifier = Modifier.size(if (isNarrow) 20.dp else 22.dp)
                            )
                        }
                    } else {
                        // Navigation Tab Capsules
                        val pillScale by animateFloatAsState(
                            targetValue = if (selected) 1.03f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "tab_scale"
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
                                .scale(pillScale)
                                .clip(RoundedCornerShape(18.dp))
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
                                }
                                .padding(
                                    horizontal = if (selected) (if (isNarrow) 8.dp else 12.dp) else (if (isNarrow) 4.dp else 8.dp),
                                    vertical = 6.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                screen.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title,
                                        tint = contentColor,
                                        modifier = Modifier.size(if (isNarrow) 18.dp else 20.dp)
                                    )
                                }

                                if (selected && !isNarrow) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = screen.title,
                                        style = NotionTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = contentColor,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
