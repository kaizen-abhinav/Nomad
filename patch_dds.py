import re

with open('app/src/main/java/com/example/ui/screens/DeadDropsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('''@Composable
private fun NfcStatusBanner(
    onSimulateTap: () -> Unit
) {''', '''@Composable
private fun NfcStatusBanner() {''')

content = re.sub(r'NfcStatusBanner\(\s*onSimulateTap = \{\s*val sample = deadDrops.firstOrNull\(\) \?: DigitalDeadDrop\([^)]*\)[^}]*\}\s*\)', 'NfcStatusBanner()', content, flags=re.MULTILINE|re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/DeadDropsScreen.kt', 'w') as f:
    f.write(content)
