package com.bandapa.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Design spec: 4dp for buttons/inputs, 8dp for cards
val BandapaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(4.dp),
    medium     = RoundedCornerShape(8.dp),
    large      = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(16.dp),
)
