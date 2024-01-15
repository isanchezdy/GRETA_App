package upm.gretaapp.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Interface to describe the navigation destinations for the app
 */
interface NavigationDestination {
    /**
     * Unique name to define the path for a composable
     */
    val route: String

    /**
     * String resource id to that contains title to be displayed for the screen.
     */
    val titleRes: Int

    /**
     * Image resource to be displayed in the navigation drawer
     */
    val icon: ImageVector
}