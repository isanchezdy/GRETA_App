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
        val binding = FragmentInfoWindowBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.infoWindow
            .apply {
                setViewCompositionStrategy(ViewCompositionStrategy.Default)
                setContent {
                    val color = MaterialTheme.colorScheme.surfaceVariant
                    GRETAAppTheme {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                color = color,
                                shape = RoundedCornerShape(30)
                            ) {
                                Row {
                                    val locationPermissionState =
                                        rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
                                    if(locationPermissionState.status.isGranted) {
                                        Button(
                                            onClick = {
                                                onClick()
                                                view.visibility = View.GONE
                                            },
                                            enabled = locationPermissionState.status.isGranted,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(text = stringResource(id = R.string.calculate_route))
                                        }
                                    } else {
                                        Text(text = stringResource(id = R.string.enable_location),
                                            modifier = Modifier.padding(8.dp)
                                                .align(Alignment.CenterVertically))
                                    }

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