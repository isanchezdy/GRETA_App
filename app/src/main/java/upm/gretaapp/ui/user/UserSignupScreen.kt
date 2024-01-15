package upm.gretaapp.ui.user

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import upm.gretaapp.R
import upm.gretaapp.ui.navigation.NavigationDestination

object SignupDestination : NavigationDestination {
    override val route = "signup"
    override val titleRes = R.string.sign_up
    override val icon: ImageVector = Icons.Filled.Abc
}

@Composable
fun UserSignupScreen(
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {

}