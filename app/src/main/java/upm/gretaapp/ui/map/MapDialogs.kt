package upm.gretaapp.ui.map

import android.annotation.SuppressLint
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
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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

    // One phrase is picked at random position
    var currentPhrase by remember { mutableStateOf(ecoDrivingPhrases.random()) }

    // Function to update the current phrase with a new one
    val calculateNewPhrase = { var newPhrase = ecoDrivingPhrases.random()
        while (newPhrase == currentPhrase) {
            newPhrase = ecoDrivingPhrases.random()
        }
        currentPhrase = newPhrase
    }

    // Counter to update the phrase every 10 seconds
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
    // It lasts 5 seconds
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
                    text = stringResource(
                        id = when(code) {
                            3 -> R.string.long_route
                            2 -> R.string.error_signup
                            else -> R.string.server_available
                        }
                    ),
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
 * @param isElectric Flag to check if the result is shown in liters or kW/h
 * @param sendFiles The function for sending the recording files through another app
 * @param clearScore Clears the information of the results from the phone to avoid them from being
 * shown multiple times
 * @param needsConsumption Flag to check if the app needs the consumption to adjust results
 * @param updateFactor Function to update the consumption factor of the vehicle if it is required
 */
@SuppressLint("DefaultLocale")
@Composable
fun ScoresResult(
    score: PerformedRouteMetrics,
    isElectric: Boolean,
    sendFiles: () -> Unit,
    clearScore: () -> Unit,
    needsConsumption: Boolean,
    updateFactor: (Double) -> Unit,
    speedVariationPhrases: List<String> =
        stringArrayResource(id = R.array.speed_variation_phrases).toList(),
    drivingSmoothnessPhrases: List<String> =
        stringArrayResource(id = R.array.driving_smoothness_phrases).toList()
) {
    var visible by remember{ mutableStateOf(true) }

    // Function to close the view
    val close = {
        visible = false
        // The score is cleared if the consumption is not required, otherwise the next popup manages that
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
                        text = stringResource(id = R.string.slow_driving),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )

                    Score(score = score.drivingAggressiveness)

                    Text(
                        when(score.drivingAggressiveness) {
                            1, 2 -> drivingSmoothnessPhrases[0]
                            3 -> drivingSmoothnessPhrases[1]
                            4, 5 -> drivingSmoothnessPhrases[2]
                            else -> drivingSmoothnessPhrases[1]
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )

                    Text(
                        text = stringResource(id = R.string.speeding),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )

                    Score(score = score.speedVariationNum)

                    Text(
                        when(score.speedVariationNum) {
                            1, 2 -> speedVariationPhrases[0]
                            3 -> speedVariationPhrases[1]
                            4, 5 -> speedVariationPhrases[2]
                            else -> speedVariationPhrases[1]
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 24.dp)
                    )


                    Text(
                        text = stringResource(id = R.string.distance) + ": " +
                                String.format("%.3f", (score.performedRouteDistance/1000.0) )
                                + " km",
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.time) + ": " +
                                ceil(score.performedRouteTime/60).toInt().toString()
                                + " min",
                        modifier = Modifier.padding(8.dp)
                    )
                    // Consumption / 100km is calculated only if the vehicle is not electric
                    Text(
                        text = stringResource(id = R.string.consumption) + ": "
                                + if(isElectric) {
                                    String.format("%.3f", score.performedRouteConsumption) + " kW/h"
                                } else {
                                    String.format("%.1f",score.performedRouteConsumption * 100.0 /
                                    (score.performedRouteDistance/1000.0)) + " l/100km\n(" +
                                            String.format("%.3f", score.performedRouteConsumption) + " l)"

                                },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )

                    Button(onClick = sendFiles, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                        Text(stringResource(id = R.string.send_results))
                    }

                    Button(onClick = close, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)) {
                        Text(stringResource(id = R.string.accept))
                    }
                }
            }
        }
    } else if(needsConsumption) {
        // The consumption of the vehicle is asked to adjust the values if required
        RegisterRealConsumption(
            updateFactor = updateFactor,
            clearScore = clearScore
        )
    }
}

/**
 * Function to show a score with stars
 *
 * @param score The score to represent
 */
@Composable
fun Score(score: Int) {
    val stars = stringResource(id = R.string.stars)
    Row(modifier = Modifier.semantics { contentDescription = "$score $stars" }) {
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
 * Popup to introduce data for calculating the routes
 *
 * @param visible Flag to show the popup only when the button has been pressed
 * @param vehicles Vehicles from the current user to select for the routes
 * @param selectedVehicle Current vehicle for the routes to estimate consumption
 * @param isElectric Flag to change the unit of the result if the vehicle is electric
 * @param numberOfPersons Number of persons inside the car
 * @param numberOfBulks Number of bags or luggage inside the car
 * @param sendFiles Function to send the recording files through another app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteParams(
    visible: MutableState<Boolean>,
    vehicles: List<Pair<UserVehicle, Vehicle>>,
    selectedVehicle: MutableState<Pair<Long, Long>?>,
    isElectric: MutableState<Boolean>,
    numberOfPersons: MutableState<Int>,
    numberOfBulks: MutableState<Int?>,
    sendFiles: () -> Unit
) {
    // Function to close the view
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

                    // Variable to check if the vehicle list is expanded or not
                    var expanded by remember { mutableStateOf(false) }

                    // Menu with all the vehicles of the user
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(0.8f)
                    ) {
                        // Field to show the current selection and update it from selecting in the list
                        TextField(
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
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

                        // Menu with all the vehicles of the user as options
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            // For each vehicle, an option is added
                            vehicles.forEach {
                                DropdownMenuItem(
                                    text = { Text(it.second.name.replace("_-_", " ")) },
                                    onClick = {
                                        // The selected vehicle for the route is updated
                                        selectedVehicle.value = Pair(it.first.id!!, it.second.vehicleID)
                                        // Checks if the vehicle is electric when the result is shown
                                        isElectric.value = it.second.motorType == "ELECTRIC"
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Field to check the number of persons inside the car
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

                    // Field to introduce the number of bags or luggage inside the car
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

                    // Button to send the recording files
                    Button(onClick = sendFiles, modifier = Modifier.padding(top = 16.dp)) {
                        Text(stringResource(id = R.string.send_results))
                    }

                    // Button to select current options
                    Button(onClick = close, modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(id = R.string.accept))
                    }
                }
            }
        }
    }
}

/**
 * Popup screen that registers consumption of the vehicle to adjust results
 *
 * @param updateFactor Function to update the consumption factor of the vehicle if it is required
 * @param clearScore Clears the information of the results from the phone to avoid them from being
 * shown multiple times
 */
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

                    // Value of the consumption
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

                            // Clears the results of previous routes to avoid inconsistencies
                            clearScore()
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }

                    // Button to save results
                    Button(
                        onClick = {
                            visible = false
                            // Updates the factor only if the result was not blank
                            if(performedConsumption100km.isNotBlank())
                                updateFactor(performedConsumption100km.toDouble())
                            // Clears the results of previous routes to avoid inconsistencies
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

@Preview(showBackground = true, heightDp = 480, widthDp = 320)
@Preview(showBackground = true, heightDp = 480, widthDp = 320, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "es")
@Composable
fun RegisterRealConsumptionPreview() {
    GRETAAppTheme {
        RegisterRealConsumption({}) {
        }
    }
}