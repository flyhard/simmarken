package se.simmarken.data.export

import kotlinx.serialization.json.Json

object ExportJson {
    val strictJson = Json {
        ignoreUnknownKeys = false
        encodeDefaults = true
    }

    val lenientJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(dto: BackupDto): String = strictJson.encodeToString(BackupDto.serializer(), dto)

    fun decode(json: String): BackupDto = lenientJson.decodeFromString(BackupDto.serializer(), json)
}
