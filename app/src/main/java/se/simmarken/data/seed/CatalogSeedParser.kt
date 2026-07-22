package se.simmarken.data.seed

import android.content.Context
import kotlinx.serialization.json.Json

object CatalogSeedParser {
    private val testJson = Json {
        ignoreUnknownKeys = true
    }

    val strictJson = Json {
        ignoreUnknownKeys = false
    }

    fun loadFromAssets(context: Context, assetPath: String): CatalogSeedDto {
        val text = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        return strictJson.decodeFromString(CatalogSeedDto.serializer(), text)
    }

    fun loadFromString(json: String): CatalogSeedDto {
        return testJson.decodeFromString(CatalogSeedDto.serializer(), json)
    }
}
