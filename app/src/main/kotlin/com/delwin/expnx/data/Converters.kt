package com.delwin.expnx.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCategory(category: Category): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(value: String): Category {
        return Category.valueOf(value)
    }

    @TypeConverter
    fun fromBillCategory(category: com.delwin.expnx.ui.screens.plans.BillCategory): String {
        return category.name
    }

    @TypeConverter
    fun toBillCategory(value: String): com.delwin.expnx.ui.screens.plans.BillCategory {
        return com.delwin.expnx.ui.screens.plans.BillCategory.valueOf(value)
    }
}
