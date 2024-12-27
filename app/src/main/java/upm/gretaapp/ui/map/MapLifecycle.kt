package upm.gretaapp.ui.map

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


/**
 * Composable with a [MapView] to keep it managed with its own lifecycle
 *
 * @return [MapView] initialized with default values
 */
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current

    // Initializes default values of the map
    val mapView by remember {
        mutableStateOf(
            MapView(context).apply {
                this.minZoomLevel = 5.0
                this.maxZoomLevel = 22.0
                this.setTileSource(TileSourceFactory.MAPNIK)
                this.setMultiTouchControls(true)
                this.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                this.controller.setZoom(18.0)
                this.controller.setCenter(GeoPoint(40.447234,-3.7348339))
            }
        )
    }

    // Makes MapView follow the lifecycle of this composable
    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

/**
 * Observes the lifecycle of the activity to manage the state of the [mapView]
 *
 * @param mapView [MapView] to manage its current state depending of the lifecycle
 * @return A [LifecycleEventObserver] that manages the state of [mapView]
 */
@Composable
fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
    }

/**
 * Composable with a [MyLocationNewOverlay] to keep it managed with its own lifecycle
 *
 * @return [MyLocationNewOverlay] with default values
 */
@Composable
fun rememberLocationOverlayWithLifecycle(mapView: MapView): MyLocationNewOverlay {
    val context = LocalContext.current

    // Initializes default values of the overlay
    val locationOverlay by remember {
        mutableStateOf(MyLocationNewOverlay(mapView).apply {
            val navigationIcon =
                BitmapFactory.decodeResource(context.resources,
                    org.osmdroid.library.R.drawable.twotone_navigation_black_48)
            if(navigationIcon != null) {
                this.setDirectionIcon(navigationIcon)
            }
        })
    }

    // Makes locationOverlay follow the lifecycle of this composable
    val lifecycleObserver = rememberLocationOverlayLifecycleObserver(locationOverlay)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return locationOverlay
}

/**
 * Observes the lifecycle of the activity to manage the state of [locationNewOverlay]
 *
 * @param locationNewOverlay [MyLocationNewOverlay] to manage its current state depending of the
 * lifecycle
 * @return A [LifecycleEventObserver] that manages the state of [locationNewOverlay]
 */
@Composable
fun rememberLocationOverlayLifecycleObserver(locationNewOverlay: MyLocationNewOverlay):
        LifecycleEventObserver =
    remember(locationNewOverlay) {
        LifecycleEventObserver{ _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> locationNewOverlay.enableMyLocation()
                Lifecycle.Event.ON_PAUSE -> locationNewOverlay.disableMyLocation()
                else -> {}
            }
        }
    }

