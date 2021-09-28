SVNToolBox
==========

Enhances IntelliJ IDEA SVN integration

## Development status
Plugin is deprecated - there will be no new releases or bug-fixes.

## Jetbrains plugin repository
[Plugin repository page](https://plugins.jetbrains.com/plugin/7321-svntoolbox)

### EAP builds
Add https://plugins.jetbrains.com/plugins/eap/7321 in **Settings > Plugins > Browse repositories... > Manage
repositories...** to receive early access builds.

## Architecture decisions record
Decisions are stored [here](./SVNToolBox/doc/arch).

## Building & running

### Using docker compose
Run ```docker-compose up``` inside ```dev-env``` directory. This will start local svn server on port 13690.
Available user account can be examined in ```dev-env/passwd``` file.

Default repository URL is ```svn://localhost:13690/repos```.

Next import sample repository into running server from ```dev-env/repo```.

### Development builds
If version set in [gradle.properties](./SVNToolBox/gradle.properties) ends with `-dev` then build datetime in UTC
timezone will be appended, current Git hash and
For example dev build on `2018-01-13 13:06:12 CET` will produce version `173.1.2-dev.20180113.120612.dfea123453`

### Useful build commands
Release build
```
gradle clean buildPlugin
```
Full verification of build
```
gradle clean check
```
Quick verification of build
```
gradle clean test
```
Update gradlew version
```
gradle wrapper --gradle-version 6.4.1 --distribution-type ALL
```

### Useful run commands
Run with previous sandbox contents
```
gradle runIde
```
Run with fresh sandbox
```
gradle clean runIde
```

### Debugging
To debug the plugin execute Gradle run configuration with `runIde` task using IDE **Debug action**.

## Logging
Plugin can log additional diagnostic information to help with issues investigation. All categories can be used in any combination.

### Debug logging
Add following line to **Help > Debug Log Settings...**
```
#zielu.svntoolbox
```

### Performance logging
Add following line to **Help > Debug Log Settings...**
```
#zielu.svntoolbox.perf:trace
```
