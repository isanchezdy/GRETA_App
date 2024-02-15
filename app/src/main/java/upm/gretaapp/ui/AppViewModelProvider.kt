package upm.gretaapp.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import upm.gretaapp.GretaApplication
import upm.gretaapp.ui.map.MapViewModel
import upm.gretaapp.ui.vehicle.VehicleListViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire GRETA App
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for VehicleListViewModel
        initializer {
            VehicleListViewModel(gretaApplication().container.vehiclesRepository)
        }
        // Initializer for MapViewModel
        initializer {
            MapViewModel(gretaApplication().container.nominatimRepository,
                gretaApplication().container.recordingRepository)
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [GretaApplication].
 */
fun CreationExtras.gretaApplication(): GretaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GretaApplication)