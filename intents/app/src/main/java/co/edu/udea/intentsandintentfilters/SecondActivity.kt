package co.edu.udea.intentsandintentfilters

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import co.edu.udea.intentsandintentfilters.ui.theme.IntentsandIntentFiltersTheme

class SecondActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Con esto recibimos los datos de un intent enviado desde otra actividad
        intent.getStringExtra(Intent.EXTRA_EMAIL)
        setContent {
            IntentsandIntentFiltersTheme {
                Text(text = "SecondActivity")
            }
        }
    }

}