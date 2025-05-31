package eu.tutorials.twocars.di

import android.app.Application
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eu.tutorials.twocars.data.datasource.local.DataStore
import eu.tutorials.twocars.data.model.SeparationTime
import eu.tutorials.twocars.util.FirebaseUtils
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(application: Application): DataStore {
        return DataStore(application)
    }

    @Provides
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance()
    }

    @Provides
    fun provideSeparationTime(remoteConfig: FirebaseRemoteConfig): SeparationTime {
        return FirebaseUtils.getSeparationTimeConfig(remoteConfig)
    }
}