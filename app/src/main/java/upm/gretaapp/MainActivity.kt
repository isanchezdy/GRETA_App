package upm.gretaapp

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import upm.gretaapp.ui.theme.GRETAAppTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set flag for keeping the device screen always on while the application is running
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // The context of the application is loaded with some parameters for osmdroid to function
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = ctx.packageName

        setContent {
            GRETAAppTheme {
                GretaApp()
            }
        }
    }
}