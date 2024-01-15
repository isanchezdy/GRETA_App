package upm.gretaapp

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import upm.gretaapp.R.string
import upm.gretaapp.ui.map.MapDestination
import upm.gretaapp.ui.navigation.GretaNavHost
import upm.gretaapp.ui.theme.GRETAAppTheme

/**
 * Top level composable that represents screens for the application.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GretaApp(navController: NavHostController = rememberNavController()) {
    GretaNavHost(navController = navController)
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
    navigateUp: () -> Unit
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
    ModalDrawerSheet {
        val items = listOf(MapDestination)
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
        items.forEachIndexed {index, it ->
            NavigationDrawerItem(
                label = { Text(text = stringResource(id = it.titleRes)) },
                icon = { Icon(imageVector = it.icon, contentDescription = null) },
                selected = (index == selectedItemIndex),
                onClick = { selectedItemIndex = index
                          onNavigate(it.route) },
                modifier = Modifier.padding(16.dp)
            )
        }

        NavigationDrawerItem(
            label = { Text(text = stringResource(id = string.logout)) },
            icon = { Icon(imageVector = Icons.Filled.Logout, contentDescription = null) },
            selected = (items.size == selectedItemIndex),
            onClick = { selectedItemIndex = items.size },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, heightDp = 650)
@Preview(showBackground = true, heightDp = 650, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "es")
@Composable
fun GretaNavigationDrawerPreview() {
    GRETAAppTheme {
        GretaNavigationDrawer({ })
    }
}


