package com.forma.app.domain.model

enum class ProFeature(
    val title: String,
    val description: String,
    val iconKey: String
) {
    AI_DAY_PLANNER(
        "AI Day Planning",
        "Generate realistic time-blocked daily schedules optimized for your peak energy hours.",
        "sparkles"
    ),
    ADVANCED_STATS(
        "Advanced Analytics & Trends",
        "Deep dive into productivity trends, energy-level breakdown, and monthly momentum curves.",
        "analytics"
    ),
    CUSTOM_THEMES(
        "Curated Aesthetic Themes",
        "Unlock Warm Coffee, Minimal Monochrome, Matcha & Oat, and OLED black themes.",
        "palette"
    ),
    EXPANDED_ICONS(
        "Expanded Icon Library",
        "Access 100+ minimal icons and curated color tags for precision tagging.",
        "category"
    ),
    HOME_SCREEN_WIDGETS(
        "Home Screen Widgets",
        "Glance at your Structured timeline and tick off habits straight from your home screen.",
        "widgets"
    )
}
