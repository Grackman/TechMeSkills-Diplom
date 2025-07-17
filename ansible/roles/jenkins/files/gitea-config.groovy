import jenkins.model.*
import org.jenkinsci.plugins.gitea.*

def giteaServers = Jenkins.get().getDescriptorByType(GiteaSCMNavigator.DescriptorImpl.class)

def giteaServer = new GiteaServer("Gitea-TMS")
giteaServer.setServerUrl("http://192.168.0.20:3000/")  // IP твоей Gitea
giteaServer.setCredentialsId("gitea-token")

giteaServers.getServers().add(giteaServer)
giteaServers.save()
