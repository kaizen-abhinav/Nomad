sed -i '/private suspend fun seedInitialSurvivalData()/,/^    }/d' app/src/main/java/com/example/ui/NomadViewModel.kt
sed -i '/seedInitialSurvivalData()/d' app/src/main/java/com/example/ui/NomadViewModel.kt
