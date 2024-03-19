package upm.gretaapp.ui.stats

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.ui.graphics.vector.ImageVector
import upm.gretaapp.R
import upm.gretaapp.ui.navigation.NavigationDestination

object RouteHistoryDestination : NavigationDestination {
    override val route = "routes"
    override val titleRes = R.string.route_history
    override val icon: ImageVector = Icons.Filled.Route
}