package com.example.bluromatic.workers
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "BlurWorker"

class BlurWorker(ctx:Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        /**Se llama a la funcion de WorkerUtil, que permite mostrar un banner de notificacion de
         * estado y notificar al usuario que inicio el Worker de desenfoque y que esta
         * desenfocando la imagen**/
        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )

        //El CoroutineWorker se ejecuta de forma predeterminada como Dispatchers.Default
        //es posible cambiarlo con la llamada withContext()
        //Dispatchers.IO se ejecuta un grupo de subprocesos especial que bloquea las operaciones IO
        return withContext(Dispatchers.IO) {

            //Esta es una funcion de utilidad que agrega una emulacion de un trabajo lento
            delay(DELAY_TIME_MILLIS)

            /**Devuelve un Resulta para indicar el estado final de la solicitud de trabajo que
         * se realiza**/
            return@withContext try {

                //Se propaga la imagen con el mapa de bits, se pasan el paquete de recursos de la
                // aplicacion y el ID de recurso de la imagen
                val picture = BitmapFactory.decodeResource(
                    applicationContext.resources,
                    R.drawable.android_cupcake
                )

                //Desenfoca el mapa de bits
                val output = blurBitmap(picture, 1)

                //Escribe un mapa de pits en un archivo temporal
                val outpuUri = writeBitmapToFile(applicationContext, output)

                //Muestra un mensaje de notificacion al usuario
                makeStatusNotification(
                    "Output is $outpuUri",
                    applicationContext
                )

                Result.success()

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