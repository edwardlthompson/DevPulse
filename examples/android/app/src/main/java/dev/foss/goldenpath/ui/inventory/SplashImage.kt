package dev.foss.goldenpath.ui.inventory

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R

@Composable
fun SplashImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.splash),
        contentDescription = stringResource(R.string.app_title),
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}
