package upm.gretaapp.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView


/**
 * Composable with a MapView to keep it managed with its own lifecycle
 *
 * @return [MapView] initialized with default values
 */
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current

    val mapView by remember {
        mutableStateOf(
            MapView(context).apply {
                this.setTileSource(TileSourceFactory.MAPNIK)
                this.setMultiTouchControls(true)
                this.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                this.controller.setZoom(18.0)
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

