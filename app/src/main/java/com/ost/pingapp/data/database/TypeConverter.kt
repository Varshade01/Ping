package com.khrd.pingapp.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TypeConverter {
    @TypeConverter
    fun fromUserHashMap(inputMap: Map<String, String>?): String {
        return if (inputMap == null) "" else Gson().toJson(inputMap)
    }

    @TypeConverter
    fun toUserHashMap(inputString: String): HashMap<String, String>?{
        return Gson().fromJson(inputString, object : TypeToken<HashMap<String, String>?>() {}.type)
    }
}
