package com.example.bluromatic.workers
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.workDataOf
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "BlurWorker"

class BlurWorker(ctx:Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {

        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)

        /**Se llama a la funcion de WorkerUtil, que permite mostrar un banner de notificacion de
         * estado y notificar al usuario que inicio el Worker de desenfoque y que esta
         * desenfocando la imagen**/
       // makeStatusNotification(
       //     applicationContext.resources.getString(R.string.blurring_image),
       //     applicationContext
       // )

        //El CoroutineWorker se ejecuta de forma predeterminada como Dispatchers.Default
        //es posible cambiarlo con la llamada withContext()
        //Dispatchers.IO se ejecuta un grupo de subprocesos especial que bloquea las operaciones IO
        return withContext(Dispatchers.IO) {

            //Esta es una funcion de utilidad que agrega una emulacion de un trabajo lento
            delay(DELAY_TIME_MILLIS)

            /**Devuelve un Resulta para indicar el estado final de la solicitud de trabajo que
         * se realiza**/
            return@withContext try {

                //Arroja un IllegalArgumentException si el primer argumento se evalua como falso
                require(!resourceUri.isNullOrBlank()){
                    val errorMessage =
                        applicationContext.resources.getString(R.string.invalid_input_uri)
                    Log.e(TAG, errorMessage)
                    errorMessage
                }

                //ContentResolver no dejara leer el contenido al que apunta el URI
                val resolver = applicationContext.contentResolver

                //Se propaga la imagen con el mapa de bits, se pasan el paquete de recursos de la
                // aplicacion y el ID de recurso de la imagen
                //val picture = BitmapFactory.decodeResource(
                //    applicationContext.resources,
                //    R.drawable.android_cupcake
                //)

                //Como la fuente de la imagen es ek URI usamos decodeStream
                val picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))

                //Desenfoca el mapa de bits
                //val output = blurBitmap(picture, 1)

                val output = blurBitmap(picture, blurLevel)

                //Escribe un mapa de pits en un archivo temporal
                val outpuUri = writeBitmapToFile(applicationContext, output)

                //Muestra un mensaje de notificacion al usuario
                makeStatusNotification(
                    "Output is $outpuUri",
                    applicationContext
                )

                //workDataOf crea un objeto de datos a partir del par clave-valor que se paso
                val outputData = workDataOf(KEY_IMAGE_URI to outpuUri.toString())

                //Result.success()

                Result.success(outputData)

            } catch (throwable: Throwable){
                Log.e (
                    TAG,
                    applicationContext.resources.getString(R.string.error_applying_blur),
                    throwable
                )
                Result.failure()
            }
        }
    }

}