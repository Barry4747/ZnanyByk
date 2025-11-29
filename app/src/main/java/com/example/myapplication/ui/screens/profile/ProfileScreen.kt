package com.example.myapplication.ui.screens.profile

import FormButtonWithDetail
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.components.buttons.AlternateButton
import com.example.myapplication.ui.components.buttons.MainButton
import com.example.myapplication.ui.components.user_components.ProfilePicture
import com.example.myapplication.viewmodel.profile.ProfileViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onEditProfile: () -> Unit = {},
    onEditLocation: () -> Unit = {},
    onBecomeTrainer: () -> Unit = {},
    onEditTrainerProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    onEditSchedule: () -> Unit = {},
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
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Box(contentAlignment = Alignment.Center) {
            val avatarResource = state.avatarUrl ?: R.drawable.user_active

            ProfilePicture(
                model = avatarResource,
                size = 120,
                borderSize = 2
            )

            Icon(
                imageVector = Icons.Default.AddCircleOutline,
                contentDescription = stringResource(R.string.add),
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
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        MainButton(
            text = stringResource(R.string.edytuj_profil_button_text),
            onClick = { onEditProfile() },
            modifier = Modifier.fillMaxWidth()
        )

        if (state.userRole == "TRAINER") {
            Spacer(modifier = Modifier.height(8.dp))

            AlternateButton(
                text = stringResource(R.string.edit_profile_button_text),
                onClick = { onEditTrainerProfile() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        if (state.userRole == "CLIENT") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.want_to_become_trainer),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                AlternateButton(
                    text = stringResource(R.string.join_us),
                    onClick = { onBecomeTrainer() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FormButtonWithDetail(
                text = stringResource(R.string.language),
                detail = stringResource(R.string.polish),
                enabled = false,
                onClick = { /* No logic */ }
            )

            FormButtonWithDetail(
                text = stringResource(R.string.localization),
                detail = state.userAddress.ifEmpty { stringResource(R.string.no_given_address) },
                onClick = { onEditLocation() }
            )

            FormButtonWithDetail(
                text = stringResource(R.string.currency),
                detail = "PLN, zł",
                enabled = false,
                onClick = { /* No logic */ }
            )
            if (state.userRole == "TRAINER") AlternateButton(
                text = "Edytuj grafik trenera",
                onClick = { onEditSchedule() },
            )

        }

        Spacer(modifier = Modifier.height(32.dp))

        MainButton(
            text = stringResource(R.string.logout),
            onClick = {
                viewModel.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}