import jenkins.model.*
import org.jenkinsci.plugins.workflow.job.*

def jenkins = Jenkins.instance

def job = new WorkflowJob(jenkins, "build-my-project")
job.definition = new org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition(
    new hudson.plugins.git.GitSCM("git@192.168.0.20:admin/project-tms.git"), // адрес Gitea-репо
    "Jenkinsfile"
)

job.addTrigger(new org.jenkinsci.plugins.gitea.GiteaPushTrigger())
job.save()
