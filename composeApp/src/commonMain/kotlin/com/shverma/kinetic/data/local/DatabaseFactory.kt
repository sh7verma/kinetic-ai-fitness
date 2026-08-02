package com.shverma.kinetic.data.local

import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Builder
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

fun createAppDatabase(builder: Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .build()
