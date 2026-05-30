package com.bandapa.core.supabase

import com.bandapa.BuildKonfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

val supabaseClient = createSupabaseClient(
    supabaseUrl = BuildKonfig.SUPABASE_URL,
    supabaseKey = BuildKonfig.SUPABASE_ANON_KEY,
) {
    install(Auth)
    install(Postgrest) {
        defaultSchema = "bandapa-main"
    }
    install(Realtime)
    install(Storage)
}
