#!groovy

import jenkins.model.*
import hudson.security.*

def instance = Jenkins.getInstance()

// Создаем пользователя
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount("admin", "malina130")
instance.setSecurityRealm(hudsonRealm)

// Разрешения
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

instance.save()
