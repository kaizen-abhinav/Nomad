import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add imports
imports_to_add = """import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.MenuBook
import com.example.ui.screens.SurvivalGuideScreen
"""
content = re.sub(r'(import androidx.compose.material.icons.filled.Sensors)', r'\1\n' + imports_to_add, content)

# Add enum value
enum_replacement = """    DEAD_DROPS("Dead Drops", Icons.Filled.Contactless, Icons.Outlined.Contactless, "nav_dead_drops"),
    BEAMER("Beamer", Icons.Filled.Share, Icons.Outlined.Share, "nav_beamer"),
    GUIDE("Guide", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_guide")"""
content = content.replace('    DEAD_DROPS("Dead Drops", Icons.Filled.Contactless, Icons.Outlined.Contactless, "nav_dead_drops"),\n    BEAMER("Beamer", Icons.Filled.Share, Icons.Outlined.Share, "nav_beamer")', enum_replacement)

# Add route in when block
when_replacement = """                NomadNavigationTab.BEAMER -> {
                    ApkBeamerScreen(viewModel = viewModel, preparedApk = preparedApk)
                }
                NomadNavigationTab.GUIDE -> {
                    SurvivalGuideScreen()
                }"""
content = content.replace("""                NomadNavigationTab.BEAMER -> {
                    ApkBeamerScreen(viewModel = viewModel, preparedApk = preparedApk)
                }""", when_replacement)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
