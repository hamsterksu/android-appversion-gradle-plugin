package com.hamsterksu.plugin.version

import groovy.text.SimpleTemplateEngine
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.text.DateFormat
import java.text.SimpleDateFormat

class VersionPlugin implements Plugin<Project> {

    def props = loadProps()
    def templateEngine = new SimpleTemplateEngine()
    VersionPluginExtension versionExt;

    def originalFlavorVersion;

    void apply(Project project) {
        def androidGradlePlugin = getAndroidPluginVersion(project)

        if (androidGradlePlugin != null && !checkAndroidVersion(androidGradlePlugin.version)) {
            throw new IllegalStateException("The Android Gradle plugin ${androidGradlePlugin.version} is not supported.")
        }

        versionExt = project.extensions.create("versionPlugin", VersionPluginExtension)
        project.afterEvaluate {
            originalFlavorVersion = [:]
            project.android.applicationVariants.matching({ it.buildType.name.matches(versionExt.buildTypesMatcher)}).all {
                if (versionExt.supportBuildNumber) {
                    appendBuildNumber2VersionName(it)
                    it.getAssemble().doLast { task ->
                        if (task.state.failure) {
                            return;
                        }
                        if(originalFlavorVersion.containsKey(it.flavorName)){
                            increaseBuildNumberVersion(it.flavorName, originalFlavorVersion[it.flavorName])
                            originalFlavorVersion.remove(it.flavorName)
                        }
                    }
                }
                appendVersionNameVersionCode(project, it)
            }
        }
    }

    def static loadProps(){
        def props = new Properties()

        def file = new File("versions.properties")
        if(!file.exists()){
            file.createNewFile()
        }else {
            file.withInputStream {
                stream -> props.load(stream)
            }
        }
        return props
    }

    def increaseBuildNumberVersion(flavorName, versionName){
        def key = "${flavorName}_${versionName}"
        def buildNumber = props.getProperty(key, "0").toInteger() + 1;

        //put new build number to props
        props[key] = buildNumber.toString()
        //store property file
        new File("versions.properties").withWriter { writer ->
            props.store(writer, null)
        }
    }

    def appendBuildNumber2VersionName(variant) {
        def flavorName = variant.flavorName;
        if(!originalFlavorVersion.containsKey(flavorName)){
            originalFlavorVersion.put(flavorName, variant.mergedFlavor.versionName);    
        }

        def key = "${flavorName}_${variant.mergedFlavor.versionName}"
        def buildNumber = props.getProperty(key, "0").toInteger()
        def buildNumberText = versionExt.buildNumberPrefix == null ? (buildNumber + 1).toString() : versionExt.buildNumberPrefix + (buildNumber + 1);
        
        variant.mergedFlavor.versionName = originalFlavorVersion[flavorName] + "." + buildNumberText;
    }

    def appendVersionNameVersionCode(Project project, variant) {

        def dateFormat = versionExt.dateFormat == null ? DateFormat.getDateInstance() : new SimpleDateFormat(versionExt.dateFormat)
        def timeFormat = versionExt.timeFormat == null ? DateFormat.getTimeInstance(DateFormat.SHORT) : new SimpleDateFormat(versionExt.timeFormat)

        Date date = new Date();
        def binding = [
						'appName':project.name,
                        'projectName':project.rootProject.name,
						'flavorName':variant.flavorName,
						'buildType':variant.buildType.name,
						'versionName':variant.versionName,
						'versionCode':variant.versionCode,
						'appPkg':variant.applicationId,
						'date':dateFormat.format(date).replaceAll('\\.', '-'),
						'time':timeFormat.format(date).replaceAll(':','-').replaceAll(' ', '-'),
                        'customName':versionExt.customNameMapping.get(variant.name, '')
        ]

        def template = versionExt.fileNameFormat == null ?
                '$appName-$flavorName-$buildType-v_$versionName-c_$versionCode-d_$date-$time'
                : versionExt.fileNameFormat
        def fileName = templateEngine.createTemplate(template).make(binding).toString();
        if (variant.buildType.zipAlignEnabled) {
            def file = variant.outputs[0].outputFile
            //def fileName = file.name.replace(".apk", fileNamePostfix)
            variant.outputs[0].outputFile = new File(file.parent, fileName + ".apk")
        }

        def file = variant.outputs[0].packageApplication.outputFile
        //def fileName = file.name.replace(".apk", fileNamePostfix)
        variant.outputs[0].packageApplication.outputFile = new File(file.parent, fileName + "-unaligned.apk")
    }

    private static final String[] SUPPORTED_ANDROID_VERSIONS = ['0.14.'];

    def static boolean checkAndroidVersion(String version) {
        for (String supportedVersion : SUPPORTED_ANDROID_VERSIONS) {
            if (version.startsWith(supportedVersion)) {
                return true
            }
        }

        return false
    }

    def getAndroidPluginVersion(project){
        return findClassPathDependencyVersion(project, 'com.android.tools.build', 'gradle')
    }

    def static findClassPathDependencyVersion(project, group, attributeId){
        return project.buildscript.configurations.classpath.dependencies.find {
            it.group != null && it.group.equals(group) && it.name.equals('gradle')
        }
    }
}