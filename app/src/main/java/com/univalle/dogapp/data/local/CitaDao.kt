package com.univalle.dogapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaDao {
    @Insert
    suspend fun insertar(cita: Cita): Long

    @Update
    suspend fun actualizar(cita: Cita): Int

    @Delete
    suspend fun eliminar(cita: Cita): Int

    @Query("SELECT * FROM citas ORDER BY id ASC")
    fun listarTodas(): Flow<List<Cita>>

    @Query("SELECT * FROM citas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Cita?
}
