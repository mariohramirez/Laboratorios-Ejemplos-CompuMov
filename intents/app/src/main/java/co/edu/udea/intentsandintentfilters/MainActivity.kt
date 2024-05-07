package co.edu.udea.intentsandintentfilters

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import co.edu.udea.intentsandintentfilters.ui.theme.IntentsandIntentFiltersTheme
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {

    private  val viewModel by viewModels<ImageViewModel> ()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntentsandIntentFiltersTheme {
                Column (
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    viewModel.uri?.let {
                    AsyncImage(model = viewModel.uri,
                        contentDescription = null)
                    }
                    Button(onClick = {
                        //Intent al que se le dara la intencion de iniciar la segunda actividad
                        //startActivity es la funcion que inicia la actividad recibiendo un intent
                        //el cual acabamos de definir, por lo cual le pasamos it
                         Intent(applicationContext, SecondActivity::class.java).also {
                            startActivity(it)
                         }

                    }) {
                        Text(text = "Click me too")

                    }
                    Button(onClick = {
                        //En este caso abriremos una actividad que se encuentra por fuera de la
                        //la aplicacion por lo cual le daremos una Action al intent, en este caso
                        //la action se refiere a iniciar la actividad principal
                        Intent(Intent.ACTION_MAIN).also {
                            //No podemos en este caso enviar it sin mas, puesto que no conocemos
                            //el contexto, por lo cual definiremos el it a partir del package de la
                            //otra aplicacion, en este ejemplo sera youtube, por lo cual podemos
                            //usar el ADB que es la forma en la que podemos comunicarnos con un
                            //dispositivo como lo seria el emulador
                            //Usamos la terminal con el comando % adb shell
                            //pm para packetmanager y luego list packages para listar todos los paquetes de
                            //todas las aplicaciones instaladas
                            //pm list packages | grep youtube filtra los packages y busca el de
                            //youtube
                            it.`package` = "com.google.android.youtube"
                            try {
                                startActivity(it)
                            }catch (e: ActivityNotFoundException) {
                                e.printStackTrace()
                            }
                        }
                    }) {
                        Text(text = "Click Me")
                    }
                    Button(onClick = {
                        //El siguiente Intent sera para enviar datos
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            //Definimos el tipo de dato
                            type = "text/plain"
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("test@test.com"))
                            putExtra(Intent.EXTRA_SUBJECT, arrayOf("This is my subject"))
                            putExtra(Intent.EXTRA_TEXT, arrayOf("This is the content of my email"))
                        }
                        //Verificamos si hay actividades o apps que pueda satisfacer los
                        // requerimientos
                        if(intent.resolveActivity(packageManager)!=null){
                            startActivity(intent)
                        }
                    }) {
                        Text(text = "Implicit Intent")
                    }

                }
            }
        }
    }

    //Se lanzara esta funcion cuandose llame a la app desde un intent externo
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        //Definimos la direccion de la imagen
        //Parcelable en este caso solo funciona con Android 34 en adelante
        //Por lo cual hacemos la verificacion primero de la version
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            //En este caso esta deprecado pero a partir de la version 34, por lo cual lo usamos
            //si la version android es anterior
            intent?.getParcelableExtra(Intent.EXTRA_STREAM)
        }
        viewModel.updateUri(uri)
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
    IntentsandIntentFiltersTheme {
        Greeting("Android")
    }
}