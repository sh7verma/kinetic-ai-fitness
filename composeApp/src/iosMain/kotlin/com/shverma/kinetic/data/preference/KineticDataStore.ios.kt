package com.shverma.kinetic.data.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath

actual fun createKineticDataStore(filePath: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { filePath.toPath() })
