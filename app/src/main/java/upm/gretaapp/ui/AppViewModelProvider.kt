package upm.gretaapp.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import upm.gretaapp.GretaApplication
import upm.gretaapp.ui.home.HomeViewModel
import upm.gretaapp.ui.map.MapViewModel
import upm.gretaapp.ui.user.UserLoginViewModel
import upm.gretaapp.ui.user.UserSignupViewModel
import upm.gretaapp.ui.vehicle.VehicleAddViewModel
import upm.gretaapp.ui.vehicle.VehicleListViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire GRETA App
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                gretaApplication().container.userSessionRepository
            )
        }
        initializer {
            UserLoginViewModel(
                gretaApplication().container.gretaRepository,
                gretaApplication().container.userSessionRepository
            )
        }
        initializer {
            UserSignupViewModel(
                gretaApplication().container.gretaRepository,
                gretaApplication().container.userSessionRepository
            )
        }
        // Initializer for VehicleListViewModel
        initializer {
            VehicleListViewModel(gretaApplication().container.userSessionRepository,
                gretaApplication().container.gretaRepository)
        }
        initializer {
            VehicleAddViewModel(gretaApplication().container.gretaRepository,
                gretaApplication().container.userSessionRepository)
        }
        // Initializer for MapViewModel
        initializer {
            MapViewModel(
                gretaApplication().container.nominatimRepository,
                gretaApplication().container.userSessionRepository,
                gretaApplication().container.gretaRepository,
                gretaApplication().container.recordingRepository
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [GretaApplication].
 */
fun CreationExtras.gretaApplication(): GretaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GretaApplication)