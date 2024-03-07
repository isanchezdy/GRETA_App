package upm.gretaapp.ui.review

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.ui.navigation.NavigationDestination
import upm.gretaapp.ui.theme.GRETAAppTheme

object ReviewDestination : NavigationDestination {
    override val route = "review"
    override val titleRes = R.string.review
    override val icon: ImageVector = Icons.Filled.StarRate
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    openMenu: () -> Unit,
) {
    Scaffold(
        topBar = {
            GretaTopAppBar(canUseMenu = true, openMenu = openMenu, navigateUp = { })
        }
    ) {
        ReviewBody(modifier = Modifier.padding(it))
    }
}

@Composable
fun ReviewBody(modifier: Modifier = Modifier) {
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

            LazyColumn(modifier = modifier.padding(16.dp).padding(bottom = 8.dp).weight(0.9f)) {
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

                        var rating by rememberSaveable { mutableFloatStateOf((0.0f)) }
                        RatingBar(rating = rating, onRatingChanged = { rating = it })

                        val comment = remember { mutableStateOf("") }
                        CommentBox(comment)
                    }
                }
            }

            Button(
                onClick = { /*TODO*/ },
                shape = MaterialTheme.shapes.small,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Text(stringResource(id = R.string.save))
            }
        }
    }
}

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

@Composable
fun CommentBox (c: MutableState<String>) {
    var content by rememberSaveable { c }
    TextField(
        value = content,
        onValueChange = { content = it },
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
        ReviewBody()
    }
}

