package co.edu.udea.compumovil.workmanager

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import co.edu.udea.compumovil.workmanager.ui.theme.WorkManagerTheme
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {

    //Servira como referencia del WorkManager
    private  lateinit var  workManager: WorkManager
    private val viewModel by viewModels<PhotoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Inicializamos pasando el contexto de la aplicacion
        workManager = WorkManager.getInstance(applicationContext)
        setContent {
            WorkManagerTheme {
                val workerResult = viewModel.workId?.let { id ->
                    //getWorkInfoByIdLiveData es algo que se activa cada que hay un cambio en el worker
                    workManager.getWorkInfoByIdLiveData(id).observeAsState().value
                }
                //Cada vez que nuestros datos de salida cambian se ejecutara esta parte del codigo
                LaunchedEffect(key1 = workerResult?.outputData) {
                    if(workerResult?.outputData != null) {
                        val filePath = workerResult.outputData.getString(
                            PhotoCompressionWorker.KEY_RESULT_PATH
                        )
                        filePath?.let{
                            val bitmap = BitmapFactory.decodeFile(it)
                            viewModel.updateUncompressedBitmap(bitmap)
                        }
                    }
                }

                Column (
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    viewModel.uncompressedUri?.let { 
                        Text(text = "Uncompressed photo: ")
                        AsyncImage(model = it, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    viewModel.compressedBitmap?.let {
                        Text(text = "Compressed photo: ")
                        Image(bitmap = it.asImageBitmap(), contentDescription = null)
                    }
                }

            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent?.getParcelableExtra(Intent.EXTRA_STREAM)
        }?: return

        //La uri que pbtenemos de la otra app
        viewModel.updateUncompressUri(uri)

        //Se hara un request al Worker para que se ejecute una sola vez
        val  request = OneTimeWorkRequestBuilder <PhotoCompressionWorker>()
            .setInputData(
                workDataOf(
                    PhotoCompressionWorker.KEY_CONTENT_URI to uri.toString(),
                    //1024 es un kilobyte al multiplicarlo por 20 serian 20 kb
                    PhotoCompressionWorker.KEY_COMPRESSION_THRESHOLD to 1024 * 20L
                )
            )//Definimos restricciones, se ejecutara el trabajo segun estas restricciones
            /*.setConstraints(Constraints(
                //Que el almacenamiento no este bajo
                requiresStorageNotLow = true*
            ))*///
            .build()

        //Nos permitira tener actualizaciones de ese worker especifico con el id
        viewModel.updateWorkId(request.id)

        //Ponemos la solicitud en cola
        workManager.enqueue(request)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WorkManagerTheme {
        Greeting("Android")
    }
}