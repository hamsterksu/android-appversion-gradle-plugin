package com.hamsterksu.plugin.version

import groovy.text.SimpleTemplateEngine
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.text.DateFormat
import java.text.SimpleDateFormat

class VersionPlugin implements Plugin<Project> {

    def versionsMap = [:]
    def props = loadProps()
    def templateEngine = new SimpleTemplateEngine()
    VersionPluginExtension versionExt;

    void apply(Project project) {
        versionExt = project.extensions.create("versionPlugin", VersionPluginExtension)
        project.afterEvaluate {
            project.android.applicationVariants.matching({ it.buildType.name.matches(versionExt.buildTypesMatcher)}).all {
                if (versionExt.supportBuildNumber) {
                    appendBuildNumber2VersionName(it)
                    it.getAssemble().doLast { task ->
                        if (task.state.failure) {
                            return;
                        }
                        increaseBuildNumberVersion(it.name)
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

    def increaseBuildNumberVersion(variantName){
        variantName = variantName.capitalize();
        //println "~ increaseBuildNumberVersion"

        def versionName = versionsMap.get(variantName)
        //println "~ for ${variantName}: ${versionName}"
        def key = "${variantName}_${versionName}"
        //println "~ current buildNumber = ${props.getProperty(key, "0")}"

        def buildNumber = props.getProperty(key, "0").toInteger() + 1;
        //println "~ new buildNumber = ${buildNumber}"

        //put new build number to props
        props[key] = buildNumber.toString()

        //store property file
        new File("versions.properties").withWriter { writer ->
            props.store(writer, null)
        }
    }

    def appendBuildNumber2VersionName(variant) {
        def variantName = variant.name.capitalize();
        //println "~ appendBuildNumber2VersionName for ${variantName}"

        versionsMap.put(variantName, variant.mergedFlavor.versionName)
        //println "~ put version ${variantName} -> ${variant.mergedFlavor.versionName}"

        def key = "${variantName}_${variant.mergedFlavor.versionName}"
        def buildNumber = props.getProperty(key, "0").toInteger()
        def buildNumberText = versionExt.buildNumberPrefix == null ? (buildNumber + 1).toString() : versionExt.buildNumberPrefix + (buildNumber + 1);

        if(variant.buildType.versionNameSuffix != null) {
            variant.buildType.versionNameSuffix = "." + buildNumberText + variant.buildType.versionNameSuffix;
        }else{
            variant.buildType.versionNameSuffix = "." + buildNumberText;
        }
        //variant.mergedFlavor.versionName = variant.mergedFlavor.versionName + "." + (buildNumber + 1)
    }

    def appendVersionNameVersionCode(Project project, variant) {
        //println "~ rename out file for <${variant.name}>"

        def dateFormat = versionExt.dateFormat == null ? DateFormat.getDateInstance() : new SimpleDateFormat(versionExt.dateFormat)
        def timeFormat = versionExt.timeFormat == null ? DateFormat.getTimeInstance(DateFormat.SHORT) : new SimpleDateFormat(versionExt.timeFormat)

        Date date = new Date();
        def binding = [
						'appName':project.name,
						'flavorName':variant.flavorName,
						'buildType':variant.buildType.name,
						'versionName':variant.versionName,
						'versionCode':variant.versionCode,
						'appPkg':variant.packageName,
						'date':dateFormat.format(date).replaceAll('\\.', '-'),
						'time':timeFormat.format(date).replaceAll(':','-').replaceAll(' ', '-')
        ]

        def template = versionExt.fileNameFormat == null ?
                '$appName-$flavorName-$buildType-v_$versionName-c_$versionCode-d_$date-$time'
                : versionExt.fileNameFormat
        def fileName = templateEngine.createTemplate(template).make(binding).toString();
        if (variant.zipAlign) {
            def file = variant.outputFile
            //def fileName = file.name.replace(".apk", fileNamePostfix)
            variant.outputFile = new File(file.parent, fileName + ".apk")
        }

        def file = variant.packageApplication.outputFile
        //def fileName = file.name.replace(".apk", fileNamePostfix)
        variant.packageApplication.outputFile = new File(file.parent, fileName + "-unaligned.apk")
    }

}
