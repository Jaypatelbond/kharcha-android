package com.kharcha.tracker.presentation.screens.legal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    title: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            val sections = if (title == "Privacy Policy") PrivacyPolicySections else TermsSections
            
            sections.forEach { section ->
                if (section.title.isNotEmpty()) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Text(
                    text = section.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class LegalSection(val title: String, val content: String)

val PrivacyPolicySections = listOf(
    LegalSection(
        title = "",
        content = "Last updated: February 2026"
    ),
    LegalSection(
        title = "1. Introduction",
        content = "Kharcha (\"we\", \"our\", or \"us\") is committed to protecting your privacy. This Privacy Policy explains how your personal information is collected, used, and disclosed by Kharcha."
    ),
    LegalSection(
        title = "2. Data Collection",
        content = """
            Local Data Storage: Kharcha is primarily a local-first application. Your financial data, transactions, and categories are stored locally on your device using an encrypted database. We do not automatically upload this data to any external server.

            Google Drive Backup: If you choose to use the Cloud Backup feature, your data is encrypted and uploaded directly to your personal Google Drive account. We do not have access to your Google Drive files or the content of your backups.
        """.trimIndent()
    ),
    LegalSection(
        title = "3. Data usage",
        content = "We use your data solely to provide the expense tracking features within the app. We do not sell, trade, or rent your personal identification information to others."
    ),
    LegalSection(
        title = "4. Analytics & Improvements",
        content = "We use Google Firebase Analytics and Crashlytics to improve app stability and user experience. These services collect anonymous usage data and crash reports. They do not access your personal financial records."
    ),
    LegalSection(
        title = "5. Security",
        content = "We implement reasonable security measures to protect your data. However, please be aware that no method of transmission over the internet or method of electronic storage is 100% secure."
    ),
    LegalSection(
        title = "6. Changes to This Policy",
        content = "We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page."
    ),
    LegalSection(
        title = "7. Contact Us",
        content = "If you have any questions about this Privacy Policy, please contact us."
    )
)

val TermsSections = listOf(
    LegalSection(
        title = "",
        content = "Last updated: February 2026"
    ),
    LegalSection(
        title = "1. Acceptance of Terms",
        content = "By accessing and using Kharcha, you accept and agree to be bound by the terms and provision of this agreement."
    ),
    LegalSection(
        title = "2. Use License",
        content = "Permission is granted to download and use the Kharcha application for personal, non-commercial transitory viewing only. This is the grant of a license, not a transfer of title."
    ),
    LegalSection(
        title = "3. Disclaimer",
        content = "The materials on Kharcha are provided \"as is\". Kharcha makes no warranties, expressed or implied, and hereby disclaims and negates all other warranties, including without limitation, implied warranties or conditions of merchantability, fitness for a particular purpose, or non-infringement of intellectual property or other violation of rights."
    ),
    LegalSection(
        title = "4. Limitations",
        content = "In no event shall Kharcha or its suppliers be liable for any damages (including, without limitation, damages for loss of data or profit, or due to business interruption) arising out of the use or inability to use the materials on Kharcha."
    ),
    LegalSection(
        title = "5. Accuracy of Materials",
        content = "The materials appearing on Kharcha could include technical, typographical, or photographic errors. Kharcha does not warrant that any of the materials on its app are accurate, complete, or current."
    ),
    LegalSection(
        title = "6. Modifications",
        content = "Kharcha may revise these terms of service for its app at any time without notice. By using this app you are agreeing to be bound by the then current version of these Terms of Service."
    ),
    LegalSection(
        title = "7. Governing Law",
        content = "These terms and conditions are governed by and construed in accordance with the laws of India and you irrevocably submit to the exclusive jurisdiction of the courts in that State or location."
    )
)
