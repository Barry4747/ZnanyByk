package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.model.users.UserLocation
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.viewmodel.profile.LocationOnboardingViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LocationOnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK private lateinit var application: Application
    @MockK private lateinit var authRepository: AuthRepository
    @MockK private lateinit var userRepository: UserRepository
    @MockK private lateinit var placesClient: PlacesClient

    private lateinit var viewModel: LocationOnboardingViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Places::class)
        every { Places.createClient(any()) } returns placesClient

        coEvery { authRepository.getCachedUser() } returns Pair("user_123", mockk())

        viewModel = LocationOnboardingViewModel(application, authRepository, userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `onSaveLocation parses Place components into UserLocation correctly`() = runTest {
        val mockPlace = mockk<Place>()
        val mockLatLng = LatLng(52.2297, 21.0122)

        val cityComponent = mockComponent("locality", "Warsaw", "WAW")
        val zipComponent = mockComponent("postal_code", "00-001", "00-001")
        val countryComponent = mockComponent("country", "Poland", "PL")
        val streetComponent = mockComponent("route", "Main Street", "Main St")

        val mockAddressComponents = mockk<AddressComponents>()
        every { mockAddressComponents.asList() } returns listOf(
            cityComponent, zipComponent, countryComponent, streetComponent
        )

        every { mockPlace.latLng } returns mockLatLng
        every { mockPlace.address } returns "Warsaw, Poland"
        every { mockPlace.addressComponents } returns mockAddressComponents

        simulatePlaceSelection(mockPlace)

        coEvery { userRepository.updateUserLocation(any(), any()) } returns Result.success(Unit)

        viewModel.onSaveLocation()
        testDispatcher.scheduler.advanceUntilIdle()

        val slot = slot<UserLocation>()
        coVerify { userRepository.updateUserLocation(eq("user_123"), capture(slot)) }

        val capturedLocation = slot.captured

        assertEquals("Warsaw", capturedLocation.city)
        assertEquals("00-001", capturedLocation.postalCode)
        assertEquals("PL", capturedLocation.country)
        assertEquals("Warsaw, Poland", capturedLocation.fullAddress)
        assertEquals(52.2297, capturedLocation.latitude, 0.0)
        assertEquals(21.0122, capturedLocation.longitude, 0.0)
    }

    @Test
    fun `onSaveLocation handles missing address components gracefully`() = runTest {
        val mockPlace = mockk<Place>()

        val countryComponent = mockComponent("country", "Germany", "DE")

        val mockAddressComponents = mockk<AddressComponents>()
        every { mockAddressComponents.asList() } returns listOf(countryComponent)

        every { mockPlace.latLng } returns LatLng(10.0, 20.0)
        every { mockPlace.address } returns "Germany"
        every { mockPlace.addressComponents } returns mockAddressComponents

        simulatePlaceSelection(mockPlace)
        coEvery { userRepository.updateUserLocation(any(), any()) } returns Result.success(Unit)

        viewModel.onSaveLocation()
        testDispatcher.scheduler.advanceUntilIdle()

        val slot = slot<UserLocation>()
        coVerify { userRepository.updateUserLocation(any(), capture(slot)) }

        val capturedLocation = slot.captured

        assertNull(capturedLocation.city)
        assertNull(capturedLocation.postalCode)
        assertEquals("DE", capturedLocation.country)
    }

    @Test
    fun `onSaveLocation handles null LatLng`() = runTest {
        val mockPlace = mockk<Place>()
        every { mockPlace.latLng } returns null
        every { mockPlace.address } returns "Somewhere"
        every { mockPlace.addressComponents } returns null

        simulatePlaceSelection(mockPlace)
        coEvery { userRepository.updateUserLocation(any(), any()) } returns Result.success(Unit)

        viewModel.onSaveLocation()
        testDispatcher.scheduler.advanceUntilIdle()

        val slot = slot<UserLocation>()
        coVerify { userRepository.updateUserLocation(any(), capture(slot)) }

        assertEquals(0.0, slot.captured.latitude, 0.0)
        assertEquals(0.0, slot.captured.longitude, 0.0)
    }

    private fun simulatePlaceSelection(mockPlace: Place) {
        val mockPrediction = mockk<com.google.android.libraries.places.api.model.AutocompletePrediction>()
        every { mockPrediction.placeId } returns "some_id"
        every { mockPrediction.getPrimaryText(any()) } returns android.text.SpannableString("Some Text")

        val mockResponse = mockk<FetchPlaceResponse>()
        every { mockResponse.place } returns mockPlace

        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        coEvery { placesClient.fetchPlace(any()).await() } returns mockResponse

        viewModel.onPredictionSelected(mockPrediction)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun mockComponent(type: String, name: String, shortName: String): AddressComponent {
        val component = mockk<AddressComponent>()
        every { component.types } returns listOf(type)
        every { component.name } returns name
        every { component.shortName } returns shortName
        return component
    }
}