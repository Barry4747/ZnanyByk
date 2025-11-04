package com.example.myapplication.ui.screens.profile

import FormButtonWithDetail
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.components.buttons.AlternateButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.viewmodel.profile.ProfileViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onEditProfile: () -> Unit = {},
    onEditLocation: () -> Unit = {},
    onBecomeTrainer: () -> Unit = {},
    onEditTrainerProfile: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.addAvatar(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = state.avatarUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(120.dp),
                error = painterResource(R.drawable.user_active)
            )
            Icon(
                imageVector = Icons.Default.AddCircleOutline,
                contentDescription = "Add",
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .clickable { launcher.launch("image/*") },
                tint = Color.Gray.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = state.userName,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.userRole == "TRAINER") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainButton(
                    text = "Edytuj profil",
                    onClick = { onEditProfile() },
                    modifier = Modifier.weight(1f)
                )
                MainButton(
                    text = "Edytuj profil trenera",
                    onClick = { onEditTrainerProfile() },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            MainButton(
                text = "Edytuj profil",
                onClick = { onEditProfile() },
                modifier = Modifier.fillMaxWidth(0.6f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        if (state.userRole == "CLIENT") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chcesz zostać trenerem?",
                    style = MaterialTheme.typography.bodyLarge
                )
                AlternateButton(text = "Dołącz do nas!", onClick = { onBecomeTrainer() })
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FormButtonWithDetail(
                text = "Język",
                detail = "Polski",
                enabled = false,
                onClick = { /* No logic */ }
            )

            FormButtonWithDetail(
                text = "Lokalizacja",
                detail = state.userAddress.ifEmpty { "Brak podanego adresu" },
                onClick = { onEditLocation() }
            )

            if (state.userRole == "TRAINER") {
                FormButtonWithDetail(
                    text = "Moja siłownia",
                    detail = "Nazwa siłowni",
                    enabled = false,
                    onClick = { /* No logic */ }
                )
            }

            FormButtonWithDetail(
                text = "Waluta",
                detail = "PLN, zł",
                enabled = false,
                onClick = { /* No logic */ }
            )

            Spacer(modifier = Modifier.height(8.dp))

            FormButtonWithDetail(
                text = "Wsparcie klienta",
                detail = "Rozwiąż swój problem",
                enabled = false,
                onClick = { /* No logic */ }
            )
        }
    }
}