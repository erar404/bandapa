package com.bandapa.navigation

sealed class Route(val path: String) {
    data object Splash      : Route("splash")
    data object Login       : Route("auth/login")
    data object SignUp      : Route("auth/signup")
    data object Dashboard   : Route("main/dashboard")
    data object Calendar    : Route("main/calendar")
    data object Bands       : Route("main/bands")
    data object CreateBand  : Route("bands/create")
    // JoinBand accepts an optional invite code from deep links: "bands/join?code={code}"
    data object JoinBand    : Route("bands/join")
    // BandDetail: "bands/detail/{bandId}"
    data object BandDetail  : Route("bands/detail")
    data object Profile     : Route("main/profile")
    data object Venues      : Route("main/venues")
    data object Conflicts   : Route("main/conflicts")
}
