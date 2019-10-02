package space.mori.fakebungee.util

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser

val gson = GsonBuilder().setPrettyPrinting().create()
val jsonParser = JsonParser()

fun Any?.serializeJsonString(): String = gson.toJson(this)
fun Any?.serializeJson(): JsonElement = serializeJsonString().parseJson()

fun String.parseJson(): JsonElement = jsonParser.parse(this)

inline fun <reified T> String.parseJsonTo(): T? = gson.fromJson(this, object : TypeToken<T>(){}.type)

fun <T> String.parseJsonTo(clazz: Class<T>): T? = gson.fromJson<T>(this, clazz)
