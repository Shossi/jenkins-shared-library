// vars/tagCreation.groovy
import jenkins.model.Jenkins
import hudson.model.ParametersDefinitionProperty

def call(String jobname, String versionType, String stage) {
    println "tagCreation called with jobname: ${jobname}, versionType: ${versionType}, stage: ${stage}"
    def TAG
    if ("${stage}" == "get") {
        TAG = getTag(jobname, versionType, "get")
    } else {
        TAG = getTag(jobname, versionType, "change")
    }
    return TAG
}

@NonCPS
def getTag(String jobname, String versionType, String stage) {
    def jenkins = Jenkins.getInstance()
    def job = Jenkins.instance.getItemByFullName(jobname)
    def TAG = "0.0.0"
    def paramsDef = job.getProperty(ParametersDefinitionProperty.class)
    if (paramsDef) {
        paramsDef.parameterDefinitions.each { param ->
            if ("PreviousVersion".equals(param.name)) {
                println "Current version is ${param.defaultValue}"
                if (stage == "get") {
                    def nextValue = getUpdatedVersion(versionType, param.defaultValue)
                    println "Next version is ${nextValue}"
                    TAG = "${nextValue}"
                } else {
                    param.defaultValue = getUpdatedVersion(versionType, param.defaultValue)
                    println "Version successfully set to ${param.defaultValue}"
                    TAG = "${param.defaultValue}"
                }
            }
        }
    }
    return TAG
}

@NonCPS
def getUpdatedVersion(String versionType, String currentVersion) {
    def split = currentVersion.split('\\.')
    switch (versionType) {
        case "Patch":
            println "Updating patch version"
            split[2] = 1 + Integer.parseInt(split[2])
            break
        case "Minor":
            println "Updating minor version"
            split[2] = 0
            split[1] = 1 + Integer.parseInt(split[1])
            break
        case "Major":
            println "Updating major version"
            split[2] = 0
            split[1] = 0
            split[0] = 1 + Integer.parseInt(split[0])
            break
    }
    return split.join('.')
}

