with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# find exact text
target = """                NomadNavigationTab.BEAMER -> {
                    ZeroStateBeamerScreen(
                        viewModel = viewModel,
                        preparedApkFile = preparedApk
                    )
                }
            }
        }
    }
}"""

replacement = """                NomadNavigationTab.BEAMER -> {
                    ZeroStateBeamerScreen(
                        viewModel = viewModel,
                        preparedApkFile = preparedApk
                    )
                }
                NomadNavigationTab.GUIDE -> {
                    SurvivalGuideScreen()
                }
            }
        }
    }
}"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Target not found")
