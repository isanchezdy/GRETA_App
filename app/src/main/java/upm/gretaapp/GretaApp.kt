package upm.gretaapp

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import upm.gretaapp.R.string
import upm.gretaapp.ui.map.MapDestination
import upm.gretaapp.ui.navigation.GretaNavHost
import upm.gretaapp.ui.theme.GRETAAppTheme
import upm.gretaapp.ui.vehicle.VehicleListDestination

/**
 * Top level composable that represents screens for the application.
 */
@Composable
fun GretaApp(
    navController: NavHostController = rememberNavController(),
) {
    // Current state of the menu to open or close
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // Menu view to select screens
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GretaNavigationDrawer(onNavigate = { route -> navController.navigate(route) })
        },
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
            }
        )

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
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(string.back_button)
                    )
                }
            }
        }
    )
}

@Composable
fun GretaNavigationDrawer(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.fillMaxWidth(0.8f)
    ) {
        // List of screens to display with the menu
        val items = listOf(MapDestination, VehicleListDestination)
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
        Divider(thickness = 16.dp)
        LazyColumn(modifier = modifier) {
            itemsIndexed(items = items) { index, it ->
                NavigationDrawerItem(
                    label = { Text(text = stringResource(id = it.titleRes)) },
                    icon = { Icon(imageVector = it.icon, contentDescription = null) },
                    selected = (index == selectedItemIndex),
                    onClick = {
                        selectedItemIndex = index
                        onNavigate(it.route)
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                NavigationDrawerItem(
                    label = { Text(text = stringResource(id = string.logout)) },
                    icon = { Icon(imageVector = Icons.Filled.Logout, contentDescription = null) },
                    selected = (items.size == selectedItemIndex),
                    onClick = { selectedItemIndex = items.size },
                    modifier = Modifier.padding(16.dp)
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
        GretaNavigationDrawer({ })
    }
}


