package com.delwin.expnx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delwin.expnx.ui.theme.*
import com.delwin.expnx.ui.AppViewModel

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val isUnread: Boolean = false,
    val amount: Double? = null,
    val isAdded: Boolean = false
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()

    LaunchedEffect(notifications) {
        notifications.forEach {
            if (it.isUnread) {
                viewModel.markNotificationAsRead(it.id)
            }
        }
    }

    Scaffold(
        containerColor = NearBlack,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = CreamText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CreamText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NearBlack)
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No Notifications",
                        tint = MutedCream,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No notifications yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = CreamText,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "When you get notifications, they'll show up here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedCream
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteNotification(notification)
                                true
                            } else {
                                false
                            }
                        }
                    )
    
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.EndToStart -> RedReveal
                                else -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(color)
                                    .padding(end = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = CreamText
                                    )
                                }
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        NotificationCard(
                            notification = notification,
                            onAddExpenseClick = {
                                viewModel.setPendingSms(notification.id, notification.amount)
                                onBack()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationItem,
    onAddExpenseClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (notification.isUnread) SurfaceDark else Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GlassSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notification.icon,
                    contentDescription = null,
                    tint = if (notification.isUnread) OliveAccent else MutedCream,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = CreamText,
                    fontWeight = if (notification.isUnread) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedCream
                )
                
                if (notification.amount != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (notification.isAdded) {
                        Text(
                            text = "Added",
                            style = MaterialTheme.typography.labelMedium,
                            color = OliveAccent,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (onAddExpenseClick != null) {
                        Button(
                            onClick = onAddExpenseClick,
                            colors = ButtonDefaults.buttonColors(containerColor = OliveAccent, contentColor = NearBlack),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Add to Expenses", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notification.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = TanAccent
                )
            }
            if (notification.isUnread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(RedReveal)
                )
            }
        }
    }
}
