package com.nightbell.app.core.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.nightbell.app.domain.model.AlarmRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class NightBellDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val _eventsFlow = MutableStateFlow<List<AlarmRecord>>(emptyList())
    val eventsFlow: Flow<List<AlarmRecord>> = _eventsFlow.asStateFlow()

    init {
        refreshFlow()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_ALARM_HISTORY (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PACKAGE_NAME TEXT NOT NULL,
                $COLUMN_SENDER TEXT NOT NULL,
                $COLUMN_PREVIEW TEXT NOT NULL,
                $COLUMN_TRIGGERED_AT INTEGER NOT NULL,
                $COLUMN_STOPPED_AT INTEGER NOT NULL,
                $COLUMN_DURATION_SECONDS INTEGER NOT NULL,
                $COLUMN_STOP_REASON TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALARM_HISTORY")
        onCreate(db)
    }

    suspend fun insertEvent(event: AlarmRecord): Long = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PACKAGE_NAME, event.packageName)
            put(COLUMN_SENDER, event.sender)
            put(COLUMN_PREVIEW, event.preview)
            put(COLUMN_TRIGGERED_AT, event.triggeredAt)
            put(COLUMN_STOPPED_AT, event.stoppedAt)
            put(COLUMN_DURATION_SECONDS, event.durationSeconds)
            put(COLUMN_STOP_REASON, event.stopReason)
        }
        val id = db.insert(TABLE_ALARM_HISTORY, null, values)
        refreshFlow()
        id
    }

    suspend fun updateEventStop(id: Long, stoppedAt: Long, stopReason: String): Unit = withContext(Dispatchers.IO) {
        val db = writableDatabase
        // Fetch start time to calculate duration
        var triggeredAt = stoppedAt
        val cursor = db.rawQuery("SELECT $COLUMN_TRIGGERED_AT FROM $TABLE_ALARM_HISTORY WHERE $COLUMN_ID = ?", arrayOf(id.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                triggeredAt = it.getLong(0)
            }
        }
        val durationSeconds = ((stoppedAt - triggeredAt) / 1000).toInt().coerceAtLeast(0)

        val values = ContentValues().apply {
            put(COLUMN_STOPPED_AT, stoppedAt)
            put(COLUMN_DURATION_SECONDS, durationSeconds)
            put(COLUMN_STOP_REASON, stopReason)
        }
        db.update(TABLE_ALARM_HISTORY, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        refreshFlow()
    }

    suspend fun getAllEvents(): List<AlarmRecord> = withContext(Dispatchers.IO) {
        queryEventsInternal(null)
    }

    suspend fun getEventsForPackage(packageName: String): List<AlarmRecord> = withContext(Dispatchers.IO) {
        queryEventsInternal(packageName)
    }

    suspend fun getLastAlarm(): AlarmRecord? = withContext(Dispatchers.IO) {
        val list = queryEventsInternal(null, limit = 1)
        list.firstOrNull()
    }

    private fun queryEventsInternal(filterPackage: String?, limit: Int = 100): List<AlarmRecord> {
        val db = readableDatabase
        val selection = if (filterPackage != null) "$COLUMN_PACKAGE_NAME = ?" else null
        val selectionArgs = if (filterPackage != null) arrayOf(filterPackage) else null
        val cursor = db.query(
            TABLE_ALARM_HISTORY,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COLUMN_TRIGGERED_AT DESC",
            limit.toString()
        )
        val records = mutableListOf<AlarmRecord>()
        cursor.use {
            while (it.moveToNext()) {
                records.add(
                    AlarmRecord(
                        id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                        packageName = it.getString(it.getColumnIndexOrThrow(COLUMN_PACKAGE_NAME)),
                        sender = it.getString(it.getColumnIndexOrThrow(COLUMN_SENDER)),
                        preview = it.getString(it.getColumnIndexOrThrow(COLUMN_PREVIEW)),
                        triggeredAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_TRIGGERED_AT)),
                        stoppedAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_STOPPED_AT)),
                        durationSeconds = it.getInt(it.getColumnIndexOrThrow(COLUMN_DURATION_SECONDS)),
                        stopReason = it.getString(it.getColumnIndexOrThrow(COLUMN_STOP_REASON))
                    )
                )
            }
        }
        return records
    }

    private fun refreshFlow() {
        val list = queryEventsInternal(null)
        _eventsFlow.value = list
    }

    companion object {
        private const val DATABASE_NAME = "nightbell.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_ALARM_HISTORY = "alarm_history"
        const val COLUMN_ID = "id"
        const val COLUMN_PACKAGE_NAME = "package_name"
        const val COLUMN_SENDER = "sender"
        const val COLUMN_PREVIEW = "preview"
        const val COLUMN_TRIGGERED_AT = "triggered_at"
        const val COLUMN_STOPPED_AT = "stopped_at"
        const val COLUMN_DURATION_SECONDS = "duration_seconds"
        const val COLUMN_STOP_REASON = "stop_reason"
    }
}
