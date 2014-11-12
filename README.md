android-appversion-gradle-plugin
================================

Easy way to add build number to your application version.

Also you can add extra data to the file name. 

##Do you want to use package name, app version name and version code in file name?##

Just use the following **fileNameFormat**: *$appPkg.$versionName($versionCode)*

**Result** - com.yourdomain.app.1.0.0.1(1).apk

##Why do you need to use brackets {} in some cases?##

**fileNameFormat** - is groovy string. be careful to use something like *$appPkg.v_$versionName*. groovy will try to find property *"v_"* in String object. and you will get *Error: > No such property: v_ for class: java.lang.String*

In this case you need to use *{}*: *${appPkg}.v_$versionName*

##How to use##

####Add plugin to dependencies####

	buildscript {
		repositories {
			mavenCentral()
		}

		dependencies{
			classpath 'com.github.hamsterksu:android-appversion-gradle-plugin:1.+'
		}
	}

####Apply plugin####

	apply plugin: 'versionPlugin'
	
####Configure plugin####

	versionPlugin{
		buildTypesMatcher = 'release'

		supportBuildNumber = true
		buildNumberPrefix = 'b'
		
		fileNameFormat = '$appPkg-v_$versionName-c_$versionCode'
	}
	
##Available options:##

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
