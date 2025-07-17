import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.impl.StringCredentialsImpl
import com.cloudbees.plugins.credentials.domains.Domain
import jenkins.model.Jenkins

def jenkins = Jenkins.getInstance()

def plugin = jenkins.pluginManager.plugins.find { it.getShortName() == 'credentials' }
if (!plugin) {
    println "❌ Плагин 'credentials' не найден!"
    return
}

def cl = plugin.classLoader

def credentialsScopeClass = cl.loadClass('com.cloudbees.plugins.credentials.CredentialsScope')
def stringCredentialsClass = cl.loadClass('com.cloudbees.plugins.credentials.impl.StringCredentialsImpl')
def domainClass = cl.loadClass('com.cloudbees.plugins.credentials.domains.Domain')

def store = jenkins.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

def existing = store.getCredentials(domainClass.global()).find { it.id == 'gitea-token' }
if (existing) {
    println "⚠️ Credential 'gitea-token' уже существует."
    return
}

def credential = stringCredentialsClass.newInstance(
    credentialsScopeClass.GLOBAL,
    "gitea-token",
    "Gitea access token",
    "3a2be8c396a5ba850484a6227d65205acf14edf2"
)

store.addCredentials(domainClass.global(), credential)
println "✅ Credential 'gitea-token' успешно создана!"
