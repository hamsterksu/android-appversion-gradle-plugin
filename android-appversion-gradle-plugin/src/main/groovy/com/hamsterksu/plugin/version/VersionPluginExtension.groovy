package com.hamsterksu.plugin.version

class VersionPluginExtension{
    /**
     * append build number to the app version.
     * plugin will generate version.property file
     */
    boolean supportBuildNumber
    String buildNumberPrefix;

    /**
     * RegExp to match build types
     */
    String buildTypesMatcher;

    /**
     * Groovy template engine string
     *
     * Available vars:
     * 1. appName
     * 2. flavorName
     * 3. buildType
     * 4. versionName
     * 5. versionCode
     * 6. appPkg
     * 7. date
     * 8. time
     *
     * Default value: $appName-$flavorName-$buildType-v_$versionName-c_$versionCode-d_$date-$time
     */
    String fileNameFormat

    /**
     * format string for SimpleDateFormat
     */
    String dateFormat;

    /**
     * format string for SimpleDateFormat
     */
    String timeFormat;
}