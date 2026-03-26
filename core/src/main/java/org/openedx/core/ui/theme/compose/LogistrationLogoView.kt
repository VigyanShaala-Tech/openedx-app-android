package org.openedx.core.ui.theme.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.openedx.core.R
import org.openedx.core.ui.theme.OpenEdXTheme

@Composable
fun LogistrationLogoView() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier
                .padding(top = 20.dp, bottom = 12.dp)
                .width(150.dp)
                .height(100.dp),
            painter = painterResource(id = R.drawable.logo_white_bg),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
    }
}

@Preview(widthDp = 375)
@Composable
fun LogistrationLogoViewPreview() {
    OpenEdXTheme {
        LogistrationLogoView()
    }
}
