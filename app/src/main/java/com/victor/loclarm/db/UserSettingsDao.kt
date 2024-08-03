package com.victor.loclarm.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.victor.loclarm.db.model.UserSettings

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings LIMIT 1")
    suspend fun getUserSettings(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userSettings: UserSettings)

    @Update
    suspend fun update(userSettings: UserSettings)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(userSettings: UserSettings)
}
