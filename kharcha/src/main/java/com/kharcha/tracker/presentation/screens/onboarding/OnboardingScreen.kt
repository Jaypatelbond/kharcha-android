package com.kharcha.tracker.presentation.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kharcha.core.designsystem.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pager Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) { iteration ->
                        val color = if (pagerState.currentPage == iteration) TealPrimary else MaterialTheme.colorScheme.surfaceVariant
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(color)
                                .size(if (pagerState.currentPage == iteration) 12.dp else 8.dp)
                        )
                    }
                }

                // Next / Finish Button
                Button(
                    onClick = {
                        scope.launch {
                            if (pagerState.currentPage < 3) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                viewModel.completeOnboarding()
                                onFinish()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (pagerState.currentPage < 3) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Get Started")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            OnboardingPage(page = page)
        }
    }
}

@Composable
fun OnboardingPage(page: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val (title, description, emoji) = when (page) {
            0 -> Triple(
                "Welcome to Kharcha",
                "Your ultimate offline expense tracker. Manage your money simply and securely.",
                "👋"
            )
            1 -> Triple(
                "Expense Books & Collections",
                "Organize expenses into separate books like Home, Travel & Personal.",
                "📚"
            )
            2 -> Triple(
                "Split w/ Friends",
                "Create groups, split bills, and sync offline using file sharing.",
                "👥"
            )
            3 -> Triple(
                "Privacy First",
                "Your data stays on your device. Backup to your own Google Drive.",
                "🔒"
            )
            else -> Triple("", "", "")
        }

        Text(
            text = emoji,
            fontSize = 80.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
