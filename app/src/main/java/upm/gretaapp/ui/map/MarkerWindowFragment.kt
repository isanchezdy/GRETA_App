package upm.gretaapp.ui.map

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import upm.gretaapp.R
import upm.gretaapp.databinding.FragmentInfoWindowBinding
import upm.gretaapp.ui.theme.GRETAAppTheme

/**
 * Fragment to show when a destination marker is clicked on the map
 *
 * @param onClick Function to search routes from the current location to this position
 */
class MarkerWindowFragment(
    private val onClick: () -> Unit
): Fragment() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // The view object of the window is retrieved
        val binding = FragmentInfoWindowBinding.inflate(inflater, container, false)
        val view = binding.root

        // The contents of the view are set
        binding.infoWindow
            .apply {
                setViewCompositionStrategy(ViewCompositionStrategy.Default)
                setContent {
                    val color = MaterialTheme.colorScheme.surfaceVariant
                    GRETAAppTheme {
                        // Column for a Bubble representation
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                color = color,
                                shape = RoundedCornerShape(30)
                            ) {
                                Row {
                                    // The location permission is checked
                                    val fineLocationPermissionState =
                                        rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
                                    val coarseLocationPermissionState =
                                        rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)
                                    /*val backgroundLocationState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                    } else {
                                        null
                                    }*/

                                    // if the location is available, allows to ask for routes
                                    if((fineLocationPermissionState.status.isGranted ||
                                        coarseLocationPermissionState.status.isGranted) /*&&
                                        (backgroundLocationState == null || backgroundLocationState.status.isGranted)*/) {
                                        Button(
                                            onClick = {
                                                onClick()
                                                view.visibility = View.GONE
                                            },
                                            enabled = fineLocationPermissionState.status.isGranted,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(text = stringResource(id = R.string.calculate_route))
                                        }
                                    } else {
                                        // It shows a warning message otherwise
                                        Text(text = stringResource(id = R.string.enable_location),
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .align(Alignment.CenterVertically))
                                    }

                                    // A button to close the window
                                    IconButton(
                                        onClick = { view.visibility = View.GONE },
                                        modifier = Modifier.align(Alignment.Top)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription =
                                                stringResource(id = R.string.close_window)
                                        )
                                    }
                                }
                            }
                            // A triangle to end the bubble figure
                            Canvas(
                                modifier = Modifier
                                    .requiredSize(16.dp)
                            ) {
                                drawPath(
                                    path = Path().apply {
                                        lineTo(size.width, 0f)
                                        lineTo(size.width / 2, size.height)
                                        close()
                                    },
                                    color = color
                                )
                            }
                        }
                    }
                }
            }

        return view
    }
}