package co.edu.udea.compumovil.retrofitejemplo.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {

    @GET("discover/movie?sort_by=popularity.desc")
    suspend fun listPopoularMovies(
        //Le damos una clave
        @Query("api_key") apiKey: String,
        //Damos un valor
        @Query("region")region: String
    )

}

object RetrofitServiceFactory{
    //Devuelve un servicio de Retrofit
    fun makeRetrofitService(): RetrofitService {
        //Damos la url base, la cual se va a repetir para todas las peticiones
        //Lo que hay dentro de Get es lo que cambia segun la peticion
        //En build.create le damos la clase a paritr de la cual queremos que se cree el objeto
        //La converter facotry va a convertir los resultados a objetos y viceversa
        return Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(RetrofitService::class.java)
    }
}