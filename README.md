# Battlecode 2023 Batory HS

This is the Battlecode 2023 Batory HS repository. It contains the code for the Batory HS team.

### Project Structure

- `README.md`
    This file.
- `build.gradle`
    The Gradle build file used to build and run players.
- `src/`
    Player source code.
- `test/`
    Player test code.
- `client/`
    Contains the client. The proper executable can be found in this folder (don't move this!)
- `build/`
    Contains compiled player code and other artifacts of the build process. Can be safely ignored.
- `matches/`
    The output folder for match files.
- `maps/`
    The default folder for custom maps.
- `gradlew`, `gradlew.bat`
    The Unix (OS X/Linux) and Windows versions, respectively, of the Gradle wrapper. These are nifty scripts that you can execute in a terminal to run the Gradle build tasks of this project. If you aren't planning to do command line development, these can be safely ignored.
- `gradle/`
    Contains files used by the Gradle wrapper scripts. Can be safely ignored.

### Useful Commands

- `./gradlew build`
    Compiles your player
- `./gradlew run`
    Runs a game with the settings in gradle.properties
- `./gradlew update`
    Update configurations for the latest version -- run this often
- `./gradlew zipForUpdate`
    Create a submittable zip file
- `./gradlew tasks`
    See what else you can do!

### Shared Array

0: Symmetry type (TODO)
1-35: Islands
36-39: HQ Locations (TODO)
40-43: HQ Carrier type
<!-- 40-43: HQ AD amount
43-46: HQ MN amount -->
59: Anchor courier target island shared array location
