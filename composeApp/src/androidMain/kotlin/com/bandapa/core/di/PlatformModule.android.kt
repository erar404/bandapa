package com.bandapa.core.di

import com.bandapa.core.notifications.AndroidNotificationService
import com.bandapa.core.notifications.NotificationService
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<NotificationService> { AndroidNotificationService(androidContext()) }
}
