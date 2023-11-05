package space.mori.fakebungee.util

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser

val gson = GsonBuilder().setPrettyPrinting().create()

fun Any?.serializeJsonString(): String = gson.toJson(this)

fun <T>parseJSON(json: String, clazz: Class<T>): T = gson.fromJson(json, clazz)

inline fun <reified T> String.parseJsonTo(): T? = gson.fromJson(this, object : TypeToken<T>(){}.type)

fun <T> String.parseJsonTo(clazz: Class<T>): T? = gson.fromJson<T>(this, clazz)
