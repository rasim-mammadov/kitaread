# KitaRead v1.1 build kickoff

This commit deliberately triggers the `Build KitaRead APK` GitHub Actions workflow
on `feature/kitaread-v1.1-quality`.

Required Gradle tasks:
- `:app:testDebugUnitTest`
- `:app:assembleDebug`

Expected artifact: `KitaRead-v1.1-quality-debug-apk`

Production v1.0 remains on `archive/kitaread-production-v1.0`,
commit `cb76e92a8ae5ec11c6cd1bf84808885a11388770`.
