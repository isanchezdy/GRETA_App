package upm.gretaapp

import android.app.AlertDialog
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import upm.gretaapp.R.string
import upm.gretaapp.ui.home.HomeDestination
import upm.gretaapp.ui.map.MapDestination
import upm.gretaapp.ui.navigation.GretaNavHost
import upm.gretaapp.ui.review.ReviewDestination
import upm.gretaapp.ui.theme.GRETAAppTheme
import upm.gretaapp.ui.vehicle.VehicleListDestination

/**
 * Top level composable that represents screens for the application.
 *
 * @param navController Controller object to keep track of the current screen
 */
@Composable
fun GretaApp(
    navController: NavHostController = rememberNavController(),
) {
    // Current state of the menu to open or close
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // Skip login flag
    val skipsLogin = remember{ mutableStateOf(true) }
    // Menu view to select screens
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GretaNavigationDrawer(onNavigate = { route ->
                scope.launch {
                    drawerState.close()
                    navController.navigate(route){
                        popUpTo(0)
                    }
                }
            },
                skipsLogin = skipsLogin
            )
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        // NavHost to select screens, always starting the app with the home screen
        GretaNavHost(
            navController = navController,
            openMenu = {
                scope.launch {
                    drawerState.apply {
                        if (isClosed) open() else close()
                    }
                }
            },
            skipsLogin = skipsLogin.value
        )

        // Closes the menu when it is open instead of going to the previous screen of the backstack
        BackHandler(
            enabled = drawerState.isOpen,
            onBack = {
                scope.launch {
                    drawerState.apply {
                        close()
                    }
                }
            }
        )
    }
}

/**
 * App bar to display title and conditionally display the menu navigation.
 *
 * @param canUseMenu Flag to know if the user can access the menu, when it is false a back arrow is
 *  displayed instead
 *  @param openMenu Function to open the menu when the button is clicked
 *  @param navigateUp Function to go to the previous screen when the back arrow button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GretaTopAppBar(
    canUseMenu: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    openMenu: () -> Unit = {},
    navigateUp: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = modifier
                        .aspectRatio(5f / 1.5f)
                )
        },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canUseMenu) {
                IconButton(onClick = openMenu) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(string.menu)
                    )
                }
            }
            else {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(string.back_button)
                    )
                }
            }
        }
    )
}

/**
 * Navigation drawer for the app to select screens
 *
 * @param onNavigate Function to go to the next screen and clear all backstack
 */
@Composable
fun GretaNavigationDrawer(
    onNavigate: (String) -> Unit,
    skipsLogin: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.fillMaxWidth(0.8f)
    ) {
        // List of screens to display with the menu
        val items = listOf(MapDestination, VehicleListDestination, /*StatsDestination,*/
            ReviewDestination)
        // Index of the last selected screen
        var selectedItemIndex by rememberSaveable {
            mutableIntStateOf(0)
        }
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = modifier
                .aspectRatio(5f / 1.5f)
        )
        HorizontalDivider(thickness = 16.dp)
        // Scrollable column with all the buttons of the menu
        LazyColumn(modifier = modifier.padding(8.dp)) {
            // For each screen a button is added
            itemsIndexed(items = items) { index, it ->
                NavigationDrawerItem(
                    label = { Text(text = stringResource(id = it.titleRes)) },
                    icon = { Icon(imageVector = it.icon, contentDescription = null) },
                    selected = (index == selectedItemIndex),
                    onClick = {
                        if(selectedItemIndex != index){
                            selectedItemIndex = index
                            onNavigate(it.route)
                        }
                    },
                    modifier = Modifier.padding(2.dp)
                )
            }

            // Button to logout from the menu
            item {
                val context = LocalContext.current
                NavigationDrawerItem(
                    label = { Text(text = stringResource(id = string.logout)) },
                    icon = { Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    selected = (items.size == selectedItemIndex),
                    onClick = {
                        val previousSelected = selectedItemIndex
                        selectedItemIndex = items.size

                        // Dialog to ask user if they really wanna quit
                        val builder = AlertDialog.Builder(context)
                        builder.setMessage(string.logout_text)
                        builder.setNegativeButton(string.no) { _, _ ->
                            selectedItemIndex = previousSelected
                        }
                        builder.setPositiveButton(string.yes) { _, _ ->
                            selectedItemIndex = 0
                            skipsLogin.value = false
                            onNavigate(HomeDestination.route)
                        }
                        builder.show()
                    },
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 650, widthDp = 400)
@Preview(showBackground = true, heightDp = 650, widthDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "es")
@Composable
fun GretaNavigationDrawerPreview() {
    GRETAAppTheme {
        GretaNavigationDrawer({ }, remember{ mutableStateOf(true) })
    }
}


