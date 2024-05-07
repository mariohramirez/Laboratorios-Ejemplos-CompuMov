package co.edu.udea.compumovil.workmanager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

//Worker que correra de manera asincrona
class PhotoCompressionWorker(
    private  val appContext: Context,
    private  val  params: WorkerParameters
): CoroutineWorker(appContext, params) {

    //Funcion que se ejecutara cuando queramos ejecutar nuestro Worker
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO){
        //Usaremos para tener la direccion de la imagen
        val stringUri = params.inputData.getString(KEY_CONTENT_URI)
        val compressionTresholdInBytes = params.inputData.getLong(
            KEY_COMPRESSION_THRESHOLD, 0L
        )
        //Ahora hacemos un Uri a partir del string
        val uri = Uri.parse(stringUri)
        //Leemos los bytes reales de la imagen
        val bytes = appContext.contentResolver.openInputStream(uri)?.use {
            //Devuelve una matriz con los byttes
            it.readBytes()
            //En caso de que no hayan bytes que leer y los bytes queden vacios
        }?:return@withContext Result.failure()

            //Creamos un mapa que decodifica los bytes, offset para comenzar desde el primero
            //y el tamano del arreglo
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            //Nos ayudara a tomar los bytes comprimidos
            var outputBytes: ByteArray
            //Sin perdida de calidad
            var quality = 100
            do{
                //Cumple con el flujo de salida de bytes
                val outputStream = ByteArrayOutputStream()
                outputStream.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    //Son los btyes comprimidos despues de la compresion anterior
                    outputBytes = outputStream.toByteArray()
                    //Restamos diez por ciento de la calidad anterior y rendondeamos
                    //Volvemos a chequear si despues de la compresion encaja en el tamano
                    //de nuestro archivo
                    quality -= (quality * 0.1).roundToInt()
                }
            }while (outputBytes.size>compressionTresholdInBytes && quality>5)

            //Obtenemos el directorio del archivo con appContext, con cacheDir obtenemos solo un
            //archivo en cache y obtenemos un nombre unico para el archivo
            val file = File(appContext.cacheDir, "${params.id}.jpg")
            file.writeBytes(outputBytes)

            Result.success(
                workDataOf(
                    KEY_RESULT_PATH to file
                )
            )
        }
    }

    //Necesitamos una key para identificar el Uri, para ello usamos este companion object
    companion object{
        const val KEY_CONTENT_URI = "KEY_CONTENT_URI"
        const val KEY_COMPRESSION_THRESHOLD = "KEY_COMPRESSION_THRESHOLD"
        const val KEY_RESULT_PATH = "KEY_RESULT_PATH"
    }
}