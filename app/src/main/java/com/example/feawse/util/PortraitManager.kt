package com.example.feawse.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.LruCache
import com.example.feawse.data.ClassDb
import com.example.feawse.data.UnitDb
import com.example.feawse.savefile.units.Unit
import java.io.InputStream

object PortraitManager {

    private val cache = object : LruCache<String, Bitmap>(100) {
        override fun sizeOf(key: String, value: Bitmap): Int = 1
    }

    private fun loadAssetBitmap(context: Context, path: String): Bitmap? {
        val cached = cache.get(path)
        if (cached != null) return cached

        return try {
            val stream: InputStream = context.assets.open(path)
            val bitmap = BitmapFactory.decodeStream(stream)
            stream.close()
            if (bitmap != null) {
                cache.put(path, bitmap)
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Loads the composite portrait bitmap for any in-game Unit.
     */
    fun getPortrait(context: Context, unit: Unit?): Bitmap? {
        if (unit == null) return getPlaceholder(context)
        val unitId = unit.unitId

        val cacheKey = "unit_${unitId}_${unit.rawBlockEnd?.hairColor ?: ""}_${unit.rawLog?.logId ?: ""}_${unit.rawLog?.fullBuild?.contentToString() ?: ""}"
        val cached = cache.get(cacheKey)
        if (cached != null) return cached

        try {
            // Logbook / Einherjar / Avatar unit
            val log = unit.rawLog
            if (log != null) {
                val fullBuild = log.fullBuild
                val build = fullBuild[0].coerceIn(0, 2)
                val face = fullBuild[1]
                val hair = fullBuild[2].coerceIn(0, 4)
                val isFemale = fullBuild[4] > 0

                // 1. DLC Unit face
                if (log.hasFaceDlc()) {
                    val dlcPath = "portrait/dlc/dlc_$face.png"
                    val dlcBmp = loadAssetBitmap(context, dlcPath)
                    if (dlcBmp != null) {
                        cache.put(cacheKey, dlcBmp)
                        return dlcBmp
                    }
                }

                // 2. Einherjar / SpotPass Card
                if (log.isEinherjar) {
                    val logId = log.logIdLastByte
                    val spPath = "portrait/spotpass/$logId.png"
                    val spBmp = loadAssetBitmap(context, spPath)
                    if (spBmp != null) {
                        cache.put(cacheKey, spBmp)
                        return spBmp
                    }
                }

                // 3. Avatar (Custom Robin MU)
                if (!log.isEinherjar && face <= 4) {
                    val genderFolder = if (isFemale) "avatar_f" else "avatar_m"
                    val buildPath = "portrait/$genderFolder/build_0${build}_0${face}.png"
                    val base = loadAssetBitmap(context, buildPath)
                    if (base != null) {
                        val hairPath = "portrait/$genderFolder/hair_0${build}_0${hair}.png"
                        val backPath = "portrait/$genderFolder/back_0${build}_0${hair}.png"
                        val hairBmp = loadAssetBitmap(context, hairPath)
                        val backBmp = loadAssetBitmap(context, backPath)

                        val composite = Bitmap.createBitmap(base.width, base.height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(composite)

                        val hairColorHex = unit.rawBlockEnd?.hairColor
                        val paint = Paint()
                        if (hairColorHex != null && hairColorHex.isNotBlank()) {
                            try {
                                val colorInt = android.graphics.Color.parseColor("#$hairColorHex")
                                paint.colorFilter = PorterDuffColorFilter(colorInt, PorterDuff.Mode.MULTIPLY)
                            } catch (ignored: Exception) {}
                        }

                        // Tint back hair
                        if (backBmp != null) {
                            canvas.drawBitmap(backBmp, 0f, 0f, paint)
                        }

                        canvas.drawBitmap(base, 0f, 0f, null)
                        if (hairBmp != null) {
                            canvas.drawBitmap(hairBmp, 0f, 0f, paint)
                        }
                        cache.put(cacheKey, composite)
                        return composite
                    }
                }
            }

            val isPlayable = UnitDb.isUnitPlayable(unitId)

            // Children units with custom hair
            if (isPlayable && UnitDb.hasUnitCustomHairColor(unitId)) {
                val basePath = "portrait/children/$unitId.png"
                val base = loadAssetBitmap(context, basePath)
                if (base != null) {
                    val backPath = "portrait/children/${unitId}_back.png"
                    val hairPath = "portrait/children/${unitId}_hair.png"
                    val back = loadAssetBitmap(context, backPath)
                    val hair = loadAssetBitmap(context, hairPath)

                    val composite = Bitmap.createBitmap(base.width, base.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(composite)

                    // Draw tinted hair back if available
                    if (back != null) {
                        val hairColorHex = unit.rawBlockEnd?.hairColor
                        val paint = Paint()
                        if (hairColorHex != null && hairColorHex.isNotBlank()) {
                            try {
                                val colorInt = android.graphics.Color.parseColor("#$hairColorHex")
                                paint.colorFilter = PorterDuffColorFilter(colorInt, PorterDuff.Mode.MULTIPLY)
                            } catch (ignored: Exception) {}
                        }
                        canvas.drawBitmap(back, 0f, 0f, paint)
                    }

                    canvas.drawBitmap(base, 0f, 0f, null)
                    if (hair != null) {
                        canvas.drawBitmap(hair, 0f, 0f, null)
                    }
                    cache.put(cacheKey, composite)
                    return composite
                }
            }

            // Normal Playable character
            if (isPlayable) {
                val path = "portrait/characters/$unitId.png"
                val bmp = loadAssetBitmap(context, path)
                if (bmp != null) {
                    cache.put(cacheKey, bmp)
                    return bmp
                }
            }

            // Monster / Risen / Enemy
            val unitClass = unit.rawBlock1?.unitClass() ?: 0
            val monsterPath = "portrait/monster/${unitClass + 1}.png"
            val monsterBmp = loadAssetBitmap(context, monsterPath)
            if (monsterBmp != null) {
                cache.put(cacheKey, monsterBmp)
                return monsterBmp
            }

            return getPlaceholder(context)
        } catch (e: Exception) {
            return getPlaceholder(context)
        }
    }

    fun getPortraitByUnitId(context: Context, unitId: Int): Bitmap? {
        val path = "portrait/characters/$unitId.png"
        return loadAssetBitmap(context, path) ?: getPlaceholder(context)
    }

    private fun getPlaceholder(context: Context): Bitmap? {
        return loadAssetBitmap(context, "portrait/characters/what.png")
    }
}
