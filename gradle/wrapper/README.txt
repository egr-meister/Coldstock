gradle-wrapper.jar is intentionally not committed to this repository.

Generate it once with a locally installed Gradle (or let Android Studio do it
automatically on first project sync):

    gradle wrapper --gradle-version 8.9

The CI workflow (.github/workflows/android-build.yml) regenerates the wrapper
before building, so no binary jar needs to be checked in.
