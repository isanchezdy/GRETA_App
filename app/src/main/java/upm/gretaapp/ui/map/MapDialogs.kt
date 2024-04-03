package upm.gretaapp.ui.map

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.text.isDigitsOnly
import kotlinx.coroutines.delay
import upm.gretaapp.R
import upm.gretaapp.model.PerformedRouteMetrics
import upm.gretaapp.model.UserVehicle
import upm.gretaapp.model.Vehicle
import upm.gretaapp.ui.theme.GRETAAppTheme
import kotlin.math.ceil

/**
 * Dialog that shows Eco-Driving phrases to the user while a query is being processed
 *
 * @param ecoDrivingPhrases Phrases shown while waiting, tapping on the screen changes the current
 * one
 */
@Composable
fun LoadingRouteDialog(ecoDrivingPhrases: List<String> =
                           stringArrayResource(id = R.array.eco_driving_messages).toList()){

    var currentPhrase by remember { mutableStateOf(ecoDrivingPhrases.random()) }

    val calculateNewPhrase = { var newPhrase = ecoDrivingPhrases.random()
        while (newPhrase == currentPhrase) {
            newPhrase = ecoDrivingPhrases.random()
        }
        currentPhrase = newPhrase
    }

    LaunchedEffect(currentPhrase) {
        delay(10000)
        calculateNewPhrase()
    }

    Dialog(onDismissRequest = { }) {
        ElevatedCard(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                calculateNewPhrase()
            }) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = currentPhrase,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
            )
        }
    }
}

/**
 * Shows an error message when it was not possible to get information from the server
 *
 * @param code The type of error to address
 */
@Composable
fun ErrorMessage(code: Int) {
    var visible by remember{ mutableStateOf(true) }
    var timeLeft by remember{ mutableIntStateOf(5) }
    // It lasts 10 seconds
    LaunchedEffect(visible) {
        while(timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        visible = false
    }

    // It shows until time runs out or the user clicks the screen
    if(visible) {
        Dialog(onDismissRequest = { visible = false }) {
            ElevatedCard(modifier = Modifier
                .fillMaxWidth(0.8f)
            ) {
                Text(
                    text = stringResource(id = if(code == 2) {
                        R.string.error_signup
                    } else R.string.server_available),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }
        }
    }
}

/**
 * The results shown when the route is finished
 *
 * @param score The [PerformedRouteMetrics] with all the results to show
 * @param sendFiles The function for sending the recording files through another app
 * @param clearScore Clears the information of the results from the phone to avoid them from being
 * shown multiple times
 */
@Composable
fun ScoresResult(
    score: PerformedRouteMetrics,
    isElectric: Boolean,
    sendFiles: () -> Unit,
    clearScore: () -> Unit,
    needsConsumption: Boolean,
    updateFactor: (Double) -> Unit
) {
    var visible by remember{ mutableStateOf(true) }
    val close = {
        visible = false
        if(!needsConsumption) {
            clearScore()
        }
    }

    if(visible) {
        Dialog(onDismissRequest = { }) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier =
                Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.results),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(16.dp)
                    )

                    Text(
                        text = stringResource(id = R.string.stops),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )

                    Score(score = score.numStopsKm)

                    Text(
                        text = stringResource(id = R.string.speeding),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )

                    Score(score = score.drivingAggressiveness)

                    Text(
                        text = stringResource(id = R.string.slow_driving),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )

                    Score(score = score.speedVariationNum)

                    Text(
                        text = stringResource(id = R.string.distance) + ": " +
                                String.format("%.3f", (score.performedRouteDistance/1000.0) )
                                + " km",
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.time) + ": " +
                                ceil(score.performedRouteTime/60).toInt().toString()
                                + " min",
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.consumption) + ": "
                                + String.format("%.3f", score.performedRouteConsumption)
                                + if(isElectric) " kW/h" else " l",
                        modifier = Modifier.padding(16.dp)
                    )

                    Button(onClick = sendFiles, modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(id = R.string.send_results))
                    }

                    Button(onClick = close, modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(id = R.string.accept))
                    }
                }
            }
        }
    } else if(needsConsumption) {
        RegisterRealConsumption(
            updateFactor = updateFactor,
            clearScore = clearScore
        )
    }
}

/**
 * Function to show a score with starts
 *
 * @param score The score to represent
 */
@Composable
fun Score(score: Int) {
    Row {
        for (i in (1..score)) {
            Icon(
                imageVector = Icons.Filled.StarRate,
                contentDescription = null,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

/**
 * Popup to introduce
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteParams(
    visible: MutableState<Boolean>,
    vehicles: List<Pair<UserVehicle, Vehicle>>,
    selectedVehicle: MutableState<Pair<Long, Long>?>,
    isElectric: MutableState<Boolean>,
    numberOfPersons: MutableState<Int>,
    numberOfBulks: MutableState<Int?>
) {
    val close = {
        visible.value = false
    }

    if(visible.value) {
        Dialog(onDismissRequest = close) {
            ElevatedCard(
                modifier = Modifier
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, modifier =
                    Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.route_options),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(16.dp)
                    )

                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(0.8f)
                    ) {
                        TextField(
                            modifier = Modifier.menuAnchor(),
                            readOnly = true,
                            value = if(selectedVehicle.value == null) {
                                ""
                            } else {
                                vehicles
                                    .first { it.second.vehicleID == selectedVehicle.value!!.second }
                                    .second.name.replace("_-_", " ")
                            },
                            onValueChange = {},
                            label = { Text(stringResource(id = R.string.selected_vehicle)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            vehicles.forEach {
                                DropdownMenuItem(
                                    text = { Text(it.second.name.replace("_-_", " ")) },
                                    onClick = {
                                        selectedVehicle.value = Pair(it.first.id!!, it.second.vehicleID)
                                        isElectric.value = it.second.motorType == "ELECTRIC"
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    TextField(
                        value = if(numberOfPersons.value == 0) "" else numberOfPersons.value.toString(),
                        onValueChange = { if(it.length < 2) {
                            if (it.isBlank()) {
                                numberOfPersons.value = 0
                            }
                            else if(it.isDigitsOnly()) {
                                numberOfPersons.value = it.toInt()
                            }
                        }},
                        label = { Text(stringResource(id = R.string.number_passengers)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(8.dp)
                    )

                    TextField(
                        value = if(numberOfBulks.value == null) "" else numberOfBulks.value.toString(),
                        onValueChange = { if(it.length <= 2) {
                            if (it.isBlank()) {
                                numberOfBulks.value = null
                            }
                            else if(it.isDigitsOnly()) {
                                numberOfBulks.value = it.toInt()
                            }
                        }},
                        label = { Text(stringResource(id = R.string.number_bulks) + " (5 kg)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { close() }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(8.dp)
                    )

                    Button(onClick = close, modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(id = R.string.accept))
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterRealConsumption(updateFactor: (Double) -> Unit, clearScore: () -> Unit) {
    var visible by remember{ mutableStateOf(true) }

    if(visible) {
        Dialog(onDismissRequest = { }) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, modifier =
                    Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.introduce_consumption),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(16.dp)
                    )

                    var performedConsumption100km by remember{ mutableStateOf("") }
                    TextField(
                        value = performedConsumption100km,
                        onValueChange = {
                            if(it.toDoubleOrNull() != null || it == "")
                                performedConsumption100km = it
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        label = { Text(stringResource(id = R.string.consumption) + " (100 km)") },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(8.dp)
                    )

                    Button(
                        onClick = {
                            visible = false
                            if(performedConsumption100km.isNotBlank())
                                updateFactor(performedConsumption100km.toDouble())
                            clearScore()
                        },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(stringResource(id = R.string.save))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 480, widthDp = 320)
@Preview(showBackground = true, heightDp = 480, widthDp = 320, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoadingRouteDialogPreview(){
    GRETAAppTheme {
        LoadingRouteDialog(listOf("Keep a constant speed", "Turn off your engine for long pauses",
            "Stay alert for changes while driving", "Remember to monitor tire pressure"))
    }
}

@Preview(showBackground = true, heightDp = 480, widthDp = 320)
@Preview(showBackground = true, heightDp = 480, widthDp = 320, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "es")
@Composable
fun ErrorMessagePreview() {
    GRETAAppTheme {
        ErrorMessage(1)
    }
}