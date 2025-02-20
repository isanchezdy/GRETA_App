package upm.gretaapp

import android.app.Application
import upm.gretaapp.data.AppContainer
import upm.gretaapp.data.AppDataContainer

class GretaApplication : Application() {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}