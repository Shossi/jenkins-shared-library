// vars/tagCreation.groovy
import jenkins.model.Jenkins
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterValue

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
    def job = jenkins.getItemByFullName(jobname)
    def TAG = "0.0.0"
    def paramsDef = job.getProperty(ParametersDefinitionProperty.class)
    if (paramsDef) {
        paramsDef.parameterDefinitions.each { param ->
            if ("PreviousVersion".equals(param.name)) {
                println "Current version is ${param.defaultValue}"
                def currentVersion = param.defaultValue
                if (stage == "get") {
                    def nextValue = getUpdatedVersion(versionType, currentVersion)
                    println "Next version is ${nextValue}"
                    TAG = "${nextValue}"
                } else {
                    def newValue = getUpdatedVersion(versionType, currentVersion)
                    updateVersionParameter(job, newValue)
                    println "Version successfully set to ${newValue}"
                    TAG = "${newValue}"
                }
            }
        }
    }
    return TAG
}

@NonCPS
def getUpdatedVersion(String versionType, String currentVersion) {
    def versionParts = currentVersion.tokenize('.')
    if (versionParts.size() != 3) {
        error "Invalid version format: ${currentVersion}"
    }

    def major = versionParts[0].toInteger()
    def minor = versionParts[1].toInteger()
    def patch = versionParts[2].toInteger()

    switch (versionType) {
        case "Patch":
            println "Updating patch version"
            patch += 1
            break
        case "Minor":
            println "Updating minor version"
            patch = 0
            minor += 1
            break
        case "Major":
            println "Updating major version"
            patch = 0
            minor = 0
            major += 1
            break
        default:
            error "Unknown version type: ${versionType}"
    }

    return "${major}.${minor}.${patch}"
}

@NonCPS
def updateVersionParameter(job, newValue) {
    def paramsDef = job.getProperty(ParametersDefinitionProperty.class)
    def param = paramsDef.getParameterDefinition("PreviousVersion")
    if (param) {
        param.defaultValue = new StringParameterValue(param.name, newValue)
        job.save()
    } else {
        println "Parameter 'PreviousVersion' not found"
    }
}
