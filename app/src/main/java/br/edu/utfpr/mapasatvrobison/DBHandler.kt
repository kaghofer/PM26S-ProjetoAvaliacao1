package br.edu.utfpr.mapasatvrobison

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "pontos_turisticos"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "tourist_spots"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_ENDERECO = "endereco"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_PHOTO = "photo"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createTableQuery())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun createTableQuery(): String {
        return """
            CREATE TABLE $TABLE_NAME (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_NAME TEXT,
                $KEY_DESCRIPTION TEXT,
                $KEY_ENDERECO TEXT, 
                $KEY_LATITUDE REAL,
                $KEY_LONGITUDE REAL,
                $KEY_PHOTO BLOB
            )
        """.trimIndent()
    }

    // Função para limpar a tabela de pontos turísticos
    fun limparTabela() {
        writableDatabase.use { db ->
            db.execSQL("DELETE FROM $TABLE_NAME")
        }
    }

    // Função para adicionar um ponto turístico
    fun adicionarPontoTuristico(ponto: PontoTuristico): Long {
        return writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(KEY_NAME, ponto.name)
                put(KEY_DESCRIPTION, ponto.description)
                put(KEY_LATITUDE, ponto.latitude)
                put(KEY_LONGITUDE, ponto.longitude)
                put(KEY_ENDERECO, ponto.endereco)
                put(KEY_PHOTO, ponto.photo)  // Foto é armazenada como BLOB
            }
            db.insert(TABLE_NAME, null, values)
        }
    }

    // Função para buscar todos os pontos turísticos cadastrados
    fun buscarPontosTuristicos(): List<PontoTuristico> {


        val pontosTuristicos = mutableListOf<PontoTuristico>()

        try {
            readableDatabase.use { db ->
                db.query(TABLE_NAME, null, null, null, null, null, null).use { cursor ->
                    while (cursor.moveToNext()) {
                        pontosTuristicos.add(cursorParaPontoTuristico(cursor))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DBHandler", "Erro ao buscar pontos turísticos: ${e.message}")
        }

        return pontosTuristicos
    }
    // Função para buscar ponto turístico pela localização

    fun buscarPontoPorLocalizacao(latitude: Double, longitude: Double): PontoTuristico? {
        var pontoTuristico: PontoTuristico? = null

        val query = """
        SELECT * FROM $TABLE_NAME 
        WHERE $KEY_LATITUDE = ? AND $KEY_LONGITUDE = ?
    """.trimIndent()

        readableDatabase.use { db ->
            db.rawQuery(query, arrayOf(latitude.toString(), longitude.toString())).use { cursor ->
                if (cursor.moveToFirst()) {
                    pontoTuristico = cursorParaPontoTuristico(cursor)
                }
            }
        }
        return pontoTuristico
    }


    // Função para converter o cursor do banco de dados em um objeto PontoTuristico
    private fun cursorParaPontoTuristico(cursor: Cursor): PontoTuristico {
        return PontoTuristico(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),
            endereco = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ENDERECO)),
            latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LATITUDE)),
            longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LONGITUDE)),
            photo = cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_PHOTO))  // Foto armazenada como blob
        )
    }

    // Função para deletar um ponto turístico pelo ID
    fun deletarPontoTuristico(id: Int): Int {
        return writableDatabase.use { db ->
            db.delete(TABLE_NAME, "$KEY_ID = ?", arrayOf(id.toString()))
        }
    }

    // Função para atualizar um ponto turístico existente
    fun atualizarPontoTuristico(ponto: PontoTuristico): Int {
        return writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(KEY_NAME, ponto.name)
                put(KEY_DESCRIPTION, ponto.description)
                put(KEY_ENDERECO, ponto.endereco)
                put(KEY_LATITUDE, ponto.latitude)
                put(KEY_LONGITUDE, ponto.longitude)
                put(KEY_PHOTO, ponto.photo)
            }
            db.update(TABLE_NAME, values, "$KEY_ID = ?", arrayOf(ponto.id.toString()))
        }
    }
}