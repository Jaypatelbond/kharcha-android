package com.kharcha.tracker.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.kharcha.tracker.MainActivity
import com.kharcha.core.designsystem.theme.TealPrimary
// import com.kharcha.tracker.R // Assuming R is generated

class KharchaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // In a real app, we would fetch data here or observe a helper
        // For now, we provide the static UI with actions.
        
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    fun WidgetContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                 // App Icon or Title
                Text(
                    text = "Kharcha",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary,
                        fontSize = 20.sp
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.defaultWeight())
            
            // Quick Add Button
            Row(
                 modifier = GlanceModifier
                     .fillMaxWidth()
                     .background(GlanceTheme.colors.primaryContainer)
                     .padding(12.dp)
                     .clickable(actionStartActivity<MainActivity>()), // Opens Main, logic inside main can handle intent
                 verticalAlignment = Alignment.CenterVertically,
                 horizontalAlignment = Alignment.CenterHorizontally
            ) {
                 // If we had a drawable
                 // Image(provider = ImageProvider(R.drawable.ic_add), contentDescription = "Add")
                 Text(
                     text = "+ Add Transaction",
                     style = TextStyle(
                         color = GlanceTheme.colors.onPrimaryContainer,
                         fontWeight = FontWeight.Medium
                     )
                 )
            }
        }
    }
}

class KharchaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KharchaWidget()
}
