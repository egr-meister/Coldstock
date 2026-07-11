package com.coldstock.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.Disclaimers
import com.coldstock.app.ui.components.InfoBanner
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.navigation.Routes

@Composable
fun OnboardingScreen(vm: ColdstockViewModel, nav: NavHostController) {
    ScreenScaffold(title = "Welcome to Coldstock") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            OnboardingDrawerIllustration()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Coldstock",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Keep a clear manual record of what is stored in your " +
                    "freezer and which items you planned to use first.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            OnboardingPoint("See your freezer as organized drawers.")
            OnboardingPoint("Add frozen products manually and place them in a drawer.")
            OnboardingPoint("Enter your own storage period and review date.")
            OnboardingPoint("Track portions and build a Use First list.")
            OnboardingPoint("Your freezer inventory stays on this device.")
            OnboardingPoint("No barcode scanner and no camera are used.")
            OnboardingPoint(
                "Coldstock does not inspect food or provide medical, " +
                    "nutritional, dietary, or food-storage advice."
            )

            Spacer(Modifier.height(16.dp))
            InfoBanner(Disclaimers.MANUAL_TRACKING)
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    vm.completeOnboarding()
                    nav.navigate(Routes.FREEZER_SETUP)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Set Up Freezer") }

            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    vm.completeOnboarding()
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Explore First") }
        }
    }
}

@Composable
private fun OnboardingPoint(text: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "•  ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Simple stacked-drawer illustration built from Compose shapes (no photos). */
@Composable
private fun OnboardingDrawerIllustration() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .fillMaxWidth(0.28f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }
    }
}
