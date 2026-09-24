package com.forma.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitTemplateTest {

    @Test
    fun curatedTemplates_haveUniqueIds() {
        val templates = HabitTemplate.CURATED_TEMPLATES
        val uniqueIds = templates.map { it.id }.toSet()
        assertEquals("Each template must have a unique ID", templates.size, uniqueIds.size)
    }

    @Test
    fun curatedTemplates_haveNonBlankFields() {
        for (template in HabitTemplate.CURATED_TEMPLATES) {
            assertTrue("Template ${template.id} title should not be blank", template.title.isNotBlank())
            assertTrue("Template ${template.id} description should not be blank", template.description.isNotBlank())
            assertTrue("Template ${template.id} scienceNote should not be blank", template.scienceNote.isNotBlank())
            assertTrue("Template ${template.id} icon should not be blank", template.icon.isNotBlank())
            assertTrue("Template ${template.id} duration should be positive", template.durationMinutes > 0)
        }
    }

    @Test
    fun toHabit_createsValidHabitWithExpectedProperties() {
        val template = HabitTemplate.CURATED_TEMPLATES.first { it.id == "mindful_sunlight_water" }
        val habit = template.toHabit()

        assertNotNull(habit.id)
        assertEquals("Morning Sunlight & Hydration", habit.name)
        assertEquals("spa", habit.icon)
        assertEquals("#D4AF37", habit.colorTag)
        assertEquals(TimeOfDay.MORNING, habit.timeOfDay)
        assertEquals(EnergyLevel.LOW, habit.energyLevel)
        assertEquals(setOf(1, 2, 3, 4, 5, 6, 7), habit.repeatDays)
        assertFalse(habit.archived)
        assertTrue(habit.isIndefinite)
        assertEquals("Immediately after stepping out of bed", habit.stackedCueText)
        assertEquals(7 * 60, habit.reminderTimeMinutes)
        assertEquals(2, habit.subtasks.size)
    }

    @Test
    fun curatedTemplates_coverAllCategories() {
        val coveredCategories = HabitTemplate.CURATED_TEMPLATES.map { it.category }.toSet()
        for (category in HabitTemplateCategory.values()) {
            assertTrue("Category $category should have curated templates", coveredCategories.contains(category))
        }
    }
}
