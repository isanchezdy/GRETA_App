package upm.gretaapp.ui.map

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import kotlinx.coroutines.flow.StateFlow
import upm.gretaapp.R
import upm.gretaapp.databinding.FragmentInfoWindowBinding
import upm.gretaapp.model.Route
import upm.gretaapp.ui.theme.GRETAAppTheme
import kotlin.math.ceil

/**
 * Fragment to show when a route is selected
 *
 * @param _uiState The state of the recording to update the button from the interface
 * @param route The route from which the data shown in the window is retrieved
 * @param onClick Function to start a route when the button is clicked
 * @param onCancel Function to cancel the recording process when the button is clicked
 */
class RouteWindowFragment(
    private val _uiState: StateFlow<RecordingUiState>,
    private val isElectric: Boolean,
    private val route: Route,
    private val onClick: () -> Unit,
    private val onCancel: () -> Unit,
): Fragment() {

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
                        // The ui state of the window is retrieved
                        val uiState by _uiState.collectAsState()
                        // Column for a Bubble representation
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                color = color,
                                shape = RoundedCornerShape(30)
                            ) {
                                // The data of the route is shown
                                Row {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = stringResource(id = R.string.distance) + ": " +
                                                String.format("%.3f", (route.distance/1000.0) )
                                                + " km"
                                        )
                                        Text(
                                            text = stringResource(id = R.string.time) + ": " +
                                                ceil(route.time/60).toInt().toString()
                                                + " min"
                                        )
                                        Text(
                                            text = stringResource(id = R.string.consumption) + ": "
                                                + String.format("%.3f", route.energyConsumption)
                                                + if(isElectric) " kW/h" else " l"
                                        )
                                        // The cancel button is shown while recording a route
                                        if(uiState is RecordingUiState.Loading) {
                                            OutlinedButton(
                                                onClick = onCancel,
                                                modifier = Modifier.padding(top = 8.dp)
                                            ) {
                                                Text(stringResource(id = R.string.cancel_route))
                                            }
                                        } else {
                                            // The recording button shows when there is no recording running
                                            Button(
                                                onClick = {
                                                    onClick()
                                                    view.visibility = View.GONE
                                                },
                                                modifier = Modifier.padding(top = 8.dp)
                                            ) {
                                                Text(stringResource(id = R.string.start_route))
                                            }
                                        }
                                    }

                                    // Button to close the window
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