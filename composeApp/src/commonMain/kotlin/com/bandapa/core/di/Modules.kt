package com.bandapa.core.di

import com.bandapa.core.supabase.supabaseClient
import com.bandapa.feature.auth.data.AuthRepository
import com.bandapa.feature.auth.data.AuthRepositoryImpl
import com.bandapa.feature.auth.ui.AuthViewModel
import com.bandapa.feature.band.data.BandRepository
import com.bandapa.feature.band.data.BandRepositoryImpl
import com.bandapa.feature.band.ui.BandDetailViewModel
import com.bandapa.feature.band.ui.BandViewModel
import com.bandapa.feature.band.ui.BandsListViewModel
import com.bandapa.feature.calendar.data.CalendarRepository
import com.bandapa.feature.calendar.data.CalendarRepositoryImpl
import com.bandapa.feature.calendar.ui.CalendarViewModel
import com.bandapa.feature.conflicts.data.ConflictsRepository
import com.bandapa.feature.conflicts.data.ConflictsRepositoryImpl
import com.bandapa.feature.conflicts.ui.ConflictsViewModel
import com.bandapa.feature.profile.data.ProfileRepository
import com.bandapa.feature.profile.data.ProfileRepositoryImpl
import com.bandapa.feature.profile.ui.ProfileViewModel
import com.bandapa.feature.venues.data.VenueRepository
import com.bandapa.feature.venues.data.VenueRepositoryImpl
import com.bandapa.feature.venues.ui.VenueViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val coreModule = module {
    single { supabaseClient }
}

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    viewModel { AuthViewModel(get()) }
}

val bandModule = module {
    single<BandRepository> { BandRepositoryImpl(get()) }
    viewModel { BandViewModel(get()) }
    viewModel { BandsListViewModel(get()) }
    viewModel { params -> BandDetailViewModel(params.get(), get(), get()) }
}

val calendarModule = module {
    single<CalendarRepository> { CalendarRepositoryImpl(get()) }
    viewModel { CalendarViewModel(get(), get(), get()) }
}

val conflictsModule = module {
    single<ConflictsRepository> { ConflictsRepositoryImpl(get()) }
    viewModel { ConflictsViewModel(get(), get()) }
}

val venueModule = module {
    single<VenueRepository> { VenueRepositoryImpl(get()) }
    viewModel { VenueViewModel(get()) }
}

val profileModule = module {
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
}

fun appModules() = listOf(
    coreModule,
    authModule,
    bandModule,
    calendarModule,
    conflictsModule,
    venueModule,
    profileModule,
)
