package com.delwin.expnx.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val MONTHLY_BUDGET = doublePreferencesKey("monthly_budget")
    }

    val monthlyBudget: Flow<Double?> = dataStore.data
        .map { preferences ->
            preferences[MONTHLY_BUDGET]
        }

    suspend fun saveBudget(budget: Double) {
        dataStore.edit { preferences ->
            preferences[MONTHLY_BUDGET] = budget
        }
    }

    suspend fun clearBudget() {
        dataStore.edit { preferences ->
            preferences.remove(MONTHLY_BUDGET)
        }
    }
}
