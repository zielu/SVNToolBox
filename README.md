SVNToolBox
==========

Enhances IntelliJ IDEA SVN integration

## Jetbrains plugin repository
[Plugin repository page](https://plugins.jetbrains.com/plugin/7321-svntoolbox)

### EAP builds
Add https://plugins.jetbrains.com/plugins/eap/7321 in **Settings > Plugins > Browse repositories... > Manage
repositories...** to receive early access builds.

## Gradle tasks
Release build
```bash
clean test buildPlugin
```
Run with previous sandbox contents
```bash
gradle runIde
```
Run with fresh sandbox
```bash
gradle clean runIde
```

## Logging
Plugin can log additional diagnostic information to help with issues investigation. All categories can be used in any combination.

### Debug logging
Add following line to **Help > Debug Log Settings...**
```
#zielu.svntoolbox
```
