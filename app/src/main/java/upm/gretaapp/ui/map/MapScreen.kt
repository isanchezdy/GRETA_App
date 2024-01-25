package upm.gretaapp.ui.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import upm.gretaapp.R
import upm.gretaapp.ui.navigation.NavigationDestination

object MapDestination : NavigationDestination {
    override val route = "map"
    override val titleRes = R.string.map
    override val icon: ImageVector = Icons.Filled.Map
}

@Composable
fun MapScreen() {

}