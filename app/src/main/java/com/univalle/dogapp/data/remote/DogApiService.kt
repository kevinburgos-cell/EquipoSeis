package com.univalle.dogapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface DogApiService {
    @GET("breeds/list/all")
    suspend fun listarRazas(): RazasResponse

    @GET("breed/{raza}/images/random")
    suspend fun obtenerImagen(@Path("raza") raza: String): ImagenResponse

    @GET("breeds/image/random")
    suspend fun imagenAleatoria(): ImagenResponse
}
