import re

with open('app/src/main/java/com/example/ui/screens/GhostRelayScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('var location by remember { mutableStateOf("") }', 'val context = androidx.compose.ui.platform.LocalContext.current\n    var location by remember { mutableStateOf(com.example.ui.getCurrentLocationString(context)) }')

with open('app/src/main/java/com/example/ui/screens/GhostRelayScreen.kt', 'w') as f:
    f.write(content)
