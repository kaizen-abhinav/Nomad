import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacement = """                NomadNavigationTab.BEAMER -> {
                    ZeroStateBeamerScreen(
                        viewModel = viewModel,
                        preparedApkFile = preparedApk
                    )
                }
                NomadNavigationTab.GUIDE -> {
                    SurvivalGuideScreen()
                }"""

content = re.sub(r'                NomadNavigationTab\.BEAMER -> \{\s*ZeroStateBeamerScreen\([^}]+\)\s*\}', replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
