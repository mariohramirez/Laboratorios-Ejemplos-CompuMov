package co.edu.udea.compumovil.retrofitejemplo.data.model

data class RemoteResult (
    val page: Int,
    val results: List<Result>,
    val total_pages: Int,
    val total_results: Int
)