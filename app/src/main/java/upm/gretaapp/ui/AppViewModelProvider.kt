package upm.gretaapp.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import upm.gretaapp.GretaApplication
import upm.gretaapp.ui.home.HomeViewModel
import upm.gretaapp.ui.map.MapViewModel
import upm.gretaapp.ui.review.ReviewViewModel
import upm.gretaapp.ui.route_history.RouteHistoryViewModel
import upm.gretaapp.ui.stats.StatsViewModel
import upm.gretaapp.ui.user.UserLoginViewModel
import upm.gretaapp.ui.user.UserSignupViewModel
import upm.gretaapp.ui.vehicle.UserVehicleAddViewModel
import upm.gretaapp.ui.vehicle.UserVehicleListViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire GRETA App
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for HomeViewModel
        initializer {
            HomeViewModel(
                gretaApplication().container.phoneSessionRepository
            )
        }
        // Initializer for UserLoginViewModel
        initializer {
            UserLoginViewModel(
                gretaApplication().container.gretaRepository,
                gretaApplication().container.phoneSessionRepository
            )
        }
        // Initializer for UserSignupViewModel
        initializer {
            UserSignupViewModel(
                gretaApplication().container.gretaRepository,
                gretaApplication().container.phoneSessionRepository
            )
        }
        // Initializer for VehicleListViewModel
        initializer {
            UserVehicleListViewModel(gretaApplication().container.phoneSessionRepository,
                gretaApplication().container.gretaRepository)
        }
        // Initializer to UserVehicleAddViewModel
        initializer {
            UserVehicleAddViewModel(gretaApplication().container.gretaRepository,
                gretaApplication().container.phoneSessionRepository)
        }
        // Initializer for MapViewModel
        initializer {
            val savedStateHandle = createSavedStateHandle()
            MapViewModel(
                gretaApplication().container.nominatimRepository,
                gretaApplication().container.phoneSessionRepository,
                gretaApplication().container.gretaRepository,
                gretaApplication().container.recordingRepository,
                gretaApplication().container.vehicleFactorRepository,
                savedStateHandle
            )
        }
        // Initializer for ReviewViewModel
        initializer {
            ReviewViewModel(
                gretaApplication().container.phoneSessionRepository,
                gretaApplication().container.gretaRepository
            )
        }
        // Initializer for StatsViewModel
        initializer {
            StatsViewModel(
                gretaApplication().container.phoneSessionRepository,
                gretaApplication().container.gretaRepository
            )
        }
        // Initializer for UserRouteViewModel
        initializer {
            RouteHistoryViewModel(
                gretaApplication().container.phoneSessionRepository,
                gretaApplication().container.gretaRepository
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