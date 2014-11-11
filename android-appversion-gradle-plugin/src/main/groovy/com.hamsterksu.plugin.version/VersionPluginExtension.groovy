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
     * 1. versionName
     * 2. versionCode
     * 3. appPkg
     * 4. date
     * 5. time
     *
     * Default value: -v_$versionName-c_$versionCode-d_$date-$time
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