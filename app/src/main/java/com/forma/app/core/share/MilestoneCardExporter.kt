package com.forma.app.core.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MilestoneCardExporter {

    // Curated quotes that feel authentic, not AI-generated
    private val quotes = listOf(
        "\u201CSmall acts, repeated with presence, quietly shape who we become.\u201D",
        "\u201CThe rhythm matters more than the speed.\u201D",
        "\u201CConsistency is a form of kindness toward your future self.\u201D",
        "\u201CEvery day you show up is a vote for the person you want to be.\u201D",
        "\u201CWhat is practiced daily becomes effortless over time.\u201D",
        "\u201CDiscipline is choosing between what you want now and what you want most.\u201D",
        "\u201CThe secret of change is to focus all energy on building the new.\u201D",
        "\u201CProgress is not always visible, but it is always happening.\u201D"
    )

    // -- Color palette (Matcha Oat) --
    private val bgColor = Color.rgb(246, 248, 244)          // #F6F8F4
    private val accentGreen = Color.rgb(77, 106, 66)        // #4D6A42
    private val accentGreenLight = Color.rgb(229, 239, 225) // #E5EFE1
    private val textDark = Color.rgb(28, 36, 27)            // #1C241B
    private val textMuted = Color.rgb(111, 122, 110)        // #6F7A6E
    private val textTertiary = Color.rgb(158, 170, 160)     // #9EAAA0
    private val borderColor = Color.rgb(221, 230, 217)      // #DDE6D9

    suspend fun generateAndShareMilestone(
        context: Context,
        habitTitle: String,
        streakDays: Int,
        userName: String = "Friend"
    ): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            val width = 1080
            val height = 1920
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val cx = width / 2f

            // ── 1. BACKGROUND ──────────────────────────────────────────
            val bgPaint = Paint().apply {
                color = bgColor
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Subtle warm radial glow in center
            val warmGlow = Paint().apply {
                shader = RadialGradient(
                    cx, height * 0.38f, width * 0.6f,
                    Color.argb(30, 229, 239, 225),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), warmGlow)

            // ── 2. OUTER FRAME ─────────────────────────────────────────
            val margin = 60f
            val framePaint = Paint().apply {
                color = Color.argb(35, 77, 106, 66)
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                isAntiAlias = true
            }
            canvas.drawRoundRect(
                RectF(margin, margin, width - margin, height - margin),
                28f, 28f, framePaint
            )

            // ── 3. BRAND WORDMARK ──────────────────────────────────────
            val wordmarkY = 200f
            val wordmarkPaint = Paint().apply {
                color = accentGreen
                textSize = 36f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.45f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("FORMA", cx, wordmarkY, wordmarkPaint)

            // Small botanical leaf accent (drawn as a simple path)
            drawLeafIcon(canvas, cx, wordmarkY + 30f, accentGreen)

            // ── 4. HERO STREAK NUMBER ──────────────────────────────────
            val streakStr = streakDays.toString()
            val numberSize = when {
                streakDays >= 1000 -> 260f
                streakDays >= 100 -> 300f
                else -> 360f
            }
            val numberPaint = Paint().apply {
                color = textDark
                textSize = numberSize
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val numberY = 680f
            canvas.drawText(streakStr, cx, numberY, numberPaint)

            // "DAYS" label beneath number
            val daysLabelPaint = Paint().apply {
                color = accentGreen
                textSize = 52f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                letterSpacing = 0.55f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("DAYS", cx, numberY + 75f, daysLabelPaint)

            // ── 5. HABIT TITLE (Auto-Wrapped) ──────────────────────────
            val titleMaxWidth = 820
            val titleTextPaint = TextPaint().apply {
                color = textDark
                textSize = when {
                    habitTitle.length > 35 -> 48f
                    habitTitle.length > 22 -> 56f
                    else -> 64f
                }
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                isAntiAlias = true
            }

            val titleLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(habitTitle, 0, habitTitle.length, titleTextPaint, titleMaxWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(0f, 1.18f)
                    .setMaxLines(2)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(habitTitle, titleTextPaint, titleMaxWidth, Layout.Alignment.ALIGN_CENTER, 1.18f, 0f, false)
            }

            val titleStartY = 870f
            canvas.save()
            canvas.translate((width - titleLayout.width) / 2f, titleStartY)
            titleLayout.draw(canvas)
            canvas.restore()

            val titleHeight = titleLayout.height.toFloat()

            // ── 6. THIN SEPARATOR LINE ─────────────────────────────────
            val separatorY = titleStartY + titleHeight + 40f
            val linePaint = Paint().apply {
                shader = LinearGradient(
                    cx - 180f, separatorY, cx + 180f, separatorY,
                    Color.TRANSPARENT, accentGreen, Shader.TileMode.CLAMP
                )
                strokeWidth = 1.5f
                isAntiAlias = true
            }
            // Left fade-in
            canvas.drawLine(cx - 180f, separatorY, cx, separatorY, linePaint)
            // Right fade-out
            val lineRight = Paint().apply {
                shader = LinearGradient(
                    cx, separatorY, cx + 180f, separatorY,
                    accentGreen, Color.TRANSPARENT, Shader.TileMode.CLAMP
                )
                strokeWidth = 1.5f
                isAntiAlias = true
            }
            canvas.drawLine(cx, separatorY, cx + 180f, separatorY, lineRight)

            // ── 7. ATTRIBUTION LINE ────────────────────────────────────
            val cleanUserName = userName.substringBefore("@")
                .replace(".", " ")
                .split(" ")
                .firstOrNull()
                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                ?: "Friend"

            val dateFormatted = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())
            val attrPaint = Paint().apply {
                color = textMuted
                textSize = 28f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val attrY = separatorY + 50f
            canvas.drawText("$cleanUserName  ·  $dateFormatted", cx, attrY, attrPaint)

            // ── 8. STAT PILLS ROW ──────────────────────────────────────
            val pillY = attrY + 60f
            val pillHeight = 44f
            val pillRadius = 22f
            val pillSpacing = 16f

            data class StatPill(val label: String, val value: String)
            val pills = listOf(
                StatPill("STREAK", "$streakDays Days"),
                StatPill("CADENCE", "Daily"),
                StatPill("VAULT", "Private")
            )

            // Measure pill widths
            val pillLabelPaint = Paint().apply {
                color = accentGreen
                textSize = 18f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.15f
                isAntiAlias = true
            }
            val pillValuePaint = Paint().apply {
                color = textDark
                textSize = 22f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }

            val pillWidths = pills.map { pill ->
                val labelW = pillLabelPaint.measureText(pill.label)
                val valueW = pillValuePaint.measureText(pill.value)
                maxOf(labelW, valueW) + 48f
            }
            val totalPillWidth = pillWidths.sum() + pillSpacing * (pills.size - 1)
            var pillX = cx - totalPillWidth / 2f

            val pillBgPaint = Paint().apply {
                color = accentGreenLight
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val pillStrokePaint = Paint().apply {
                color = Color.argb(50, 77, 106, 66)
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
                isAntiAlias = true
            }

            // Draw each row as label above, value below
            val pillTotalHeight = 70f
            pills.forEachIndexed { index, pill ->
                val w = pillWidths[index]
                val rect = RectF(pillX, pillY, pillX + w, pillY + pillTotalHeight)
                canvas.drawRoundRect(rect, pillRadius, pillRadius, pillBgPaint)
                canvas.drawRoundRect(rect, pillRadius, pillRadius, pillStrokePaint)

                pillLabelPaint.textAlign = Paint.Align.CENTER
                pillValuePaint.textAlign = Paint.Align.CENTER
                val pillCx = pillX + w / 2f
                canvas.drawText(pill.label, pillCx, pillY + 26f, pillLabelPaint)
                canvas.drawText(pill.value, pillCx, pillY + 54f, pillValuePaint)

                pillX += w + pillSpacing
            }

            // ── 9. QUOTE ───────────────────────────────────────────────
            val selectedQuote = quotes[streakDays % quotes.size]
            val quoteMaxWidth = 760
            val quoteTextPaint = TextPaint().apply {
                color = textMuted
                textSize = 30f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                isAntiAlias = true
            }

            val quoteLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(selectedQuote, 0, selectedQuote.length, quoteTextPaint, quoteMaxWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(0f, 1.3f)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(selectedQuote, quoteTextPaint, quoteMaxWidth, Layout.Alignment.ALIGN_CENTER, 1.3f, 0f, false)
            }

            val quoteY = pillY + pillTotalHeight + 80f
            canvas.save()
            canvas.translate((width - quoteLayout.width) / 2f, quoteY)
            quoteLayout.draw(canvas)
            canvas.restore()

            // ── 10. FOOTER ─────────────────────────────────────────────
            val footerPaint = Paint().apply {
                color = textTertiary
                textSize = 22f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                letterSpacing = 0.25f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("forma  ·  serene daily rituals", cx, height - margin - 40f, footerPaint)

            // ── 11. SAVE & SHARE ───────────────────────────────────────
            val shareDir = File(context.cacheDir, "shared")
            if (!shareDir.exists()) shareDir.mkdirs()
            val imageFile = File(shareDir, "forma_milestone_${System.currentTimeMillis()}.png")

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
                putExtra(Intent.EXTRA_TEXT, "$streakDays days of $habitTitle — tracked with Forma.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Milestone")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            Result.success(chooser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Draws a small botanical leaf accent below the wordmark.
     * Simple two-stroke leaf with a center vein.
     */
    private fun drawLeafIcon(canvas: Canvas, cx: Float, cy: Float, color: Int) {
        val paint = Paint().apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        val leafSize = 18f

        // Left leaf curve
        val leftPath = Path().apply {
            moveTo(cx, cy + leafSize)
            quadTo(cx - leafSize * 1.2f, cy - leafSize * 0.3f, cx, cy - leafSize)
        }
        canvas.drawPath(leftPath, paint)

        // Right leaf curve
        val rightPath = Path().apply {
            moveTo(cx, cy + leafSize)
            quadTo(cx + leafSize * 1.2f, cy - leafSize * 0.3f, cx, cy - leafSize)
        }
        canvas.drawPath(rightPath, paint)

        // Center vein
        canvas.drawLine(cx, cy + leafSize * 0.6f, cx, cy - leafSize * 0.6f, paint.apply {
            strokeWidth = 1.5f
            this.color = Color.argb(120, 77, 106, 66)
        })
    }
}
