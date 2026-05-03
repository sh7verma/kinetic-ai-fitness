package com.shverma.kinetic.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.shverma.kinetic.data.model.UserProfileData
import com.shverma.kinetic.ui.components.KineticSecondaryButton
import com.shverma.kinetic.ui.theme.KineticTheme

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val colors = KineticTheme.colors
    val userProfile by viewModel.userProfile.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.LogoutSuccess -> onLogout()
                is ProfileEvent.DeleteAccountSuccess -> onLogout()
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account? This action is permanent and will delete all your data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            style = KineticTheme.typography.displayMd,
            color = colors.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        userProfile?.let { profile ->
            ProfileInfoSection(profile)
        } ?: Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text("Loading profile...", color = colors.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(32.dp))

        KineticSecondaryButton(
            text = "Logout",
            onClick = { viewModel.logout() },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { showDeleteConfirmation = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Delete Account",
                color = Color.Red,
                style = KineticTheme.typography.labelLg
            )
        }
    }
}

@Composable
fun ProfileInfoSection(profile: UserProfileData) {
    val colors = KineticTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        ProfileItem(label = "Name", value = profile.name)
        ProfileItem(label = "Age", value = "${profile.age.roundToInt()}")
        ProfileItem(label = "Sex", value = profile.sex)
        ProfileItem(label = "Weight", value = "${profile.weight.roundToInt()} ${profile.weightUnit}")
        ProfileItem(label = "Height", value = "${profile.height.roundToInt()} ${profile.heightUnit}")
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Goals",
            style = KineticTheme.typography.titleMd,
            color = colors.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        ProfileItem(label = "Workout Goal", value = profile.workoutGoal)
        ProfileItem(label = "Activity Level", value = profile.activityLevel)
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    val colors = KineticTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = KineticTheme.typography.bodyLg,
            color = colors.onSurfaceVariant
        )
        Text(
            text = value,
            style = KineticTheme.typography.bodyLg,
            color = colors.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
    Divider(color = colors.outlineVariant, thickness = 0.5.dp)
}
