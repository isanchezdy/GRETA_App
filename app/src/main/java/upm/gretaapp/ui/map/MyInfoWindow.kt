package upm.gretaapp.ui.map

import android.view.View
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow

/**
 * Implementation of the class [InfoWindow] to place information when an item from the map is clicked
 */
class MyInfoWindow(
    view: View,
    mapView: MapView
): InfoWindow(view, mapView) {
    override fun onOpen(item: Any?) {
        view.visibility = View.VISIBLE
    }

    override fun onClose() {
    }

}