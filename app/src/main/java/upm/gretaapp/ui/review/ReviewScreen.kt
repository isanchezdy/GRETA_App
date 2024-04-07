package upm.gretaapp.ui.review

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.ui.AppViewModelProvider
import upm.gretaapp.ui.navigation.NavigationDestination
import upm.gretaapp.ui.theme.GRETAAppTheme

/**
 * Object that represents the route of the Review screen
 */
object ReviewDestination : NavigationDestination {
    override val route = "review"
    override val titleRes = R.string.review
    override val icon: ImageVector = Icons.Filled.StarRate
}

/**
 * Composable that represents the screen to send reviews of the app
 *
 * @param openMenu Function to open the menu of the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    openMenu: () -> Unit,
    viewModel: ReviewViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    Scaffold(
        topBar = {
            GretaTopAppBar(canUseMenu = true, openMenu = openMenu, navigateUp = { })
        }
    ) {
        val uiState by viewModel.uiState.collectAsState()
        ReviewBody(
            uiState = uiState,
            sendScores = viewModel::sendReviews,
            modifier = Modifier.padding(it)
        )
    }
}

/**
 * Body of the review screen with all the input boxes and functions
 *
 * @param uiState Object that represents the current state of the ui
 * @param sendScores Function to send the scores of the reviews to the server
 */
@Composable
fun ReviewBody(
    uiState: ReviewUiState,
    sendScores: (List<Pair<Int, String>>) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.review),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()) {
            // List of labels to evaluate
            val scoreLabels = listOf(
                stringResource(id = R.string.accesibility),
                stringResource(id = R.string.response_time),
                stringResource(id = R.string.configurability),
                stringResource(id = R.string.usability),
                stringResource(id = R.string.trustability),
                stringResource(id = R.string.robustness),
                stringResource(id = R.string.utility),
                stringResource(id = R.string.global_score)
            )

            // Values for each score
            val scores = remember {
                mutableStateListOf(
                    Pair(0,""),
                    Pair(0,""),
                    Pair(0,""),
                    Pair(0,""),
                    Pair(0,""),
                    Pair(0,""),
                    Pair(0,""),
                    Pair(0,"")
                )
            }

            LazyColumn(modifier = modifier
                .padding(16.dp)
                .padding(bottom = 8.dp)
                .weight(0.9f)) {
                // For each label, a score section is added
                itemsIndexed(items = scoreLabels) { index, it ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = if (index == 0) 0.dp else 32.dp)
                        )

                        // The individual rating of each label
                        var rating by rememberSaveable { mutableFloatStateOf((0.0f)) }
                        RatingBar(
                            rating = rating,
                            onRatingChanged = {
                                rating = it
                                scores[index] = scores[index].copy(first = rating.toInt())
                            }
                        )

                        // A comment box to add suggestions or opinions
                        val comment = remember { mutableStateOf("") }
                        CommentBox(comment)
                        // Every time a comment changes, it gets updated
                        LaunchedEffect(comment.value) {
                            scores[index] = scores[index].copy(second = comment.value)
                        }
                    }
                }
            }
            // Depending of the state of the ui, the contents of the screen change
            when (uiState) {
                // When the sending process is loading, a circular indicator appears
                is ReviewUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                }
                // When the review has been sent, a message appears
                is ReviewUiState.Complete -> {
                    Text(
                        text = stringResource(R.string.review_sent),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                else -> {
                    // If an error happens, an error message is shown
                    if(uiState is ReviewUiState.Error) {
                        Text(
                            text = stringResource(id = if(uiState.code == 2) {
                                R.string.error_signup
                            } else R.string.server_available),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    // Button to send the reviews
                    Button(
                        onClick = { sendScores(scores) },
                        shape = MaterialTheme.shapes.small,
                        enabled = scores.all { it.first > 0 },
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                    ) {
                        Text(stringResource(id = R.string.save))
                    }
                }
            }
        }
    }
}

/**
 * Composable that represents a clickable rating bar with 5 stars
 *
 * @param rating The number of stars to highlight the score
 * @param maxRating The maximum number of starts used to rate, default 5
 * @param onRatingChanged Function to update the [rating] when clicking a star with the score
 */
@Composable
fun RatingBar(
    rating: Float,
    maxRating: Int = 5,
    onRatingChanged: (Float) -> Unit
) {
    Row(modifier = Modifier.padding(8.dp)) {
        for (i in 1..maxRating) {
            IconButton(onClick = { onRatingChanged(i.toFloat()) }) {
                Icon(imageVector = if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = i.toString())
            }
        }
    }
}

/**
 * Composable that represents a text box to introduce comments
 *
 * @param c Comment introduced in the text box
 */
@Composable
fun CommentBox (c: MutableState<String>) {
    var content by rememberSaveable { c }
    TextField(
        value = content,
        onValueChange = { content = it.trimStart() },
        label = { Text(stringResource(id = R.string.comments)) },
        singleLine = false,
        minLines = 5,
        maxLines = 8,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true, heightDp = 650)
@Preview(showBackground = true, heightDp = 650, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "es")
@Composable
fun VehicleAddScreenPreview() {
    GRETAAppTheme {
        ReviewBody(ReviewUiState.Default, {})
    }
}

