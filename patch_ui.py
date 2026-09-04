import re

with open('app/src/main/java/com/example/ui/screens/GhostRelayScreen.kt', 'r') as f:
    content = f.read()

# Replace the button code
button_code = """
                            Button(
                                onClick = { viewModel.triggerCourierEncounter() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ImmersiveAccent,
                                    contentColor = ImmersiveSurface
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("SIMULATE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
"""

content = content.replace(button_code, "")

with open('app/src/main/java/com/example/ui/screens/GhostRelayScreen.kt', 'w') as f:
    f.write(content)
