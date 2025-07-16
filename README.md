# RoadVibe

RoadVibe-android is a library for an android mobile device. Target is to track all route for road pavement condition via accelerometer and gyroscope. Contains Location tracking and track uploading to remote telemetry service.

## Building

### Build ###
This project uses gradle as main building solution, so for better and faster solution it would be great to use Android Studio.

### Secrets ###
To test application or generate `library` need to generate secrets with credentials for remote services.

If there is no created roadvibe and roadvibeapp credentials properties, they can be created with this:
```powershell
Create-CredentialsFiles.ps1 [-Overwrite]
```
After that when all needed files generated, you should change values of properties in both files to your specific credentials for remote services.

```roadvibe.credentials.properties``` 

```roadvibeapp.credentials.properties```

After that you can generate secrets via this command:
```powershell
Generate-GradleCredentials.ps1
```

### Library generating ###
First of all need to prepare build for debug or just test application on real device. See [Building section](#building) section, closely [Secrets](#secrets).

All done, now it can be easily run with, it will build under release by default
```powershell
./gradlew roadvibe:generateRepo
```

Generated repo can be found at ```./roadvibe/build/distributions```

And that's it!

## Application
First of all need to prepare build for debug or just test application on real device. See [Building section](#building) section, closely [Secrets](#secrets).

After all, connect android device to your computer and check this:

![device](./resources/device.png?raw=true "Working device")

It should contain your device's name.

All preparations done, now it possible to just click on Debug or Play button to run test RoadVibe App on your device!

## Installation
It will be distributed by archived maven module. ```com.spark.roadvibe.zip```, it will contain package with structured folders
```
com
    - spark
        - roadvibe
        
            ...
            
            - <version_folder>
```

After that, need to extract this folder structure and place to your local repository, add it to your gradle build.
Or you can place ```.aar``` file to app's libs folder and added it manually by ```implmentation(roadvibe-<version>.aar)```

## Usage
The library has special context which manages start points for RoadVibe service.

From at any point of your Application you can use ```startRvs``` and inject required functions, such as ```androidContext``` as follow

```kotlin
...

startRvs {
    // Reference Android context
    androidContext(this@MainActivity)
    // Log Rvs into Android logger
    androidLogger()
    // Pass and force to use application's store implementation
    applicationTelemetryRepository()
    // Pass and force to use application's implementation of location providers
    applicationLocation()
    // Pass and force to use application's implementation of coroutineScope, as to use threads
    applicationCoroutineScope()
}

...
```
A result of this function you can use in your DI as you want.

Optional you can implement ```TrackerRepository``` on by own with this interface.
```kotlin
interface TrackerRepository {
    suspend fun getTotal(): Int
    suspend fun getScope(scopeId: UUID, limit: Int): List<TelemetryPoint>
    suspend fun getScopeIds(): List<Pair<UUID, Int>>
    suspend fun saveScope(scopeId: UUID, data: List<TelemetryPoint>)
    suspend fun remove(ids: List<Long>)
}
```

And call to ```applicationTelemetryRepository()``` with an implementation.

Also you can implement ```ApplicationLocationCallback``` to use your location implementation as follows:
```kotlin
interface ApplicationLocationCallback {
    fun locationChanged(location: Location)
}
```

And after that call ```applicationLocation()``` to start use your implementation.

There is option to force use application's implementation CoroutineScope, like no as it use library by default.
It can simply done with ```applicationCoroutineScope()```, otherwise, couroutine scope will be created by using: ```Executors.newSingleThreadScheduledExecutor()```

The result of the startRvs is ```RoadVibeService```, which follows as: 
```kotlin
interface RoadVibeService : DisposableHandle {
    /**
     * Flows of live data from sensors.
     * Does not depend from recording state
     */
    val sensors: Flow<SensorsData>

    /**
     * StateFlows of live data of remote state.
     * Does not depend from recording state
     */
    val remoteStatus: StateFlow<RemoteState>

    /**
     * Gets current state of recording state.
     */
    val state: RvsState

    /**
     * Begins recording by changing state, if not started before
     * @throws IllegalStateException when location permissions is not granted
     */
    @Throws(IllegalStateException::class)
    fun beginRecord()

    /**
     * Pauses recording by changing state, but scope will be not cleared
     */
    fun pauseRecord()

    /**
     * Finalizes recording by changing state, if not finished before
     */
    fun finishRecord()

    /**
     * Uploads recorded telemetry at any state
     * Uses internal executor to use suspend function.
     */
    fun uploadTelemetry()
}
```

All of these properties indicates of library state. You can subscribe on them and use in your application.

```beginRecord()``` - start new recording scope, if not started before. Checks location permissions and throws exception if not granted.

```pauseRecord()``` - sets pause to record, scope will not flushed.

```finishRecord()``` - sets stop to record. scope will be flushed.

```uploadTelemetry()``` - call internal uploader to uploaded all tracked data. Internal uploader uses created executor to use suspend function.

## Licensing
Not distributed freely, need to contact with contributor
