package com.forma.app.core.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MilestoneCardExporter {

    suspend fun generateAndShareMilestone(
        context: Context,
        habitTitle: String,
        streakDays: Int,
        userName: String = "Alex"
    ): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            val width = 1080
            val height = 1920
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // 1. Background Oat Canvas (#F7F8F4)
            val bgPaint = Paint().apply {
                color = Color.rgb(247, 248, 244)
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // 2. Decorative Japanese Wabi-Sabi Outer Border
            val borderPaint = Paint().apply {
                color = Color.argb(40, 78, 101, 66) // Soft matcha border
                style = Paint.Style.STROKE
                strokeWidth = 6f
                isAntiAlias = true
            }
            val margin = 80f
            canvas.drawRoundRect(RectF(margin, margin, width - margin, height - margin), 48f, 48f, borderPaint)

            // Inner Accent Border
            val innerBorder = Paint().apply {
                color = Color.argb(25, 78, 101, 66)
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            canvas.drawRoundRect(RectF(margin + 20f, margin + 20f, width - margin - 20f, height - margin - 20f), 36f, 36f, innerBorder)

            // 3. Top Stamp Watermark: "FORMA • RITUAL MASTERY"
            val topLabelPaint = Paint().apply {
                color = Color.rgb(78, 101, 66)
                textSize = 36f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.25f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("FORMA  •  MINDFUL RHYTHM", width / 2f, 260f, topLabelPaint)

            // 4. Central Ensō Circle (Matcha Green Ring)
            val ensoPaint = Paint().apply {
                color = Color.rgb(78, 101, 66)
                style = Paint.Style.STROKE
                strokeWidth = 24f
                isAntiAlias = true
            }
            val centerY = 780f
            val radius = 260f
            canvas.drawCircle(width / 2f, centerY, radius, ensoPaint)

            // Large Streak Number inside Ensō
            val streakNumberPaint = Paint().apply {
                color = Color.rgb(40, 48, 36)
                textSize = 210f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(streakDays.toString(), width / 2f, centerY + 65f, streakNumberPaint)

            val daysLabelPaint = Paint().apply {
                color = Color.rgb(78, 101, 66)
                textSize = 42f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.2f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("DAYS IN FLOW", width / 2f, centerY + 160f, daysLabelPaint)

            // 5. Habit Title & Dedication
            val habitTitlePaint = Paint().apply {
                color = Color.rgb(30, 36, 28)
                textSize = 72f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(habitTitle, width / 2f, 1260f, habitTitlePaint)

            val subtitlePaint = Paint().apply {
                color = Color.rgb(110, 115, 105)
                textSize = 38f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val dateFormatted = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())
            canvas.drawText("Ritual sustained by $userName  •  $dateFormatted", width / 2f, 1340f, subtitlePaint)

            // 6. Zen Quote & Philosophy
            val quotePaint = Paint().apply {
                color = Color.rgb(78, 101, 66)
                textSize = 38f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("“Small habits repeated with presence shape destiny.”", width / 2f, 1540f, quotePaint)

            // 7. Footer Brand Seal
            val footerPaint = Paint().apply {
                color = Color.rgb(160, 165, 155)
                textSize = 30f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.15f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("CRAFTED WITH FORMA", width / 2f, 1750f, footerPaint)

            // 8. Save to cache and construct share intent
            val shareDir = File(context.cacheDir, "shared")
            if (!shareDir.exists()) shareDir.mkdirs()
            val imageFile = File(shareDir, "milestone_${System.currentTimeMillis()}.png")

            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "Celebrating $streakDays days of mindful flow with $habitTitle on Forma.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Mindful Milestone")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            Result.success(chooser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
