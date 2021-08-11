import groovy.json.JsonOutput

def version = "UNKNOWN"
def commit = "UNKNOWN"
def dockerHubRepo = "overture/rollcall"
def gitHubRegistry = "ghcr.io"
def gitHubRepo = "overture-stack/rollcall"

pipeline {
    agent {
        kubernetes {
            label 'rollcall-executor'
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: jdk
    tty: true
    image: openjdk:11
    env:
      - name: DOCKER_HOST
        value: tcp://localhost:2375
  - name: helm
    image: alpine/helm:2.12.3
    command:
    - cat
    tty: true
  - name: docker
    image: docker:18-git
    tty: true
    env:
    - name: DOCKER_HOST
      value: tcp://localhost:2375
    - name: HOME
      value: /home/jenkins/agent
  - name: dind-daemon
    image: docker:18.06-dind
    securityContext:
        privileged: true
        runAsUser: 0
    volumeMounts:
      - name: docker-graph-storage
        mountPath: /var/lib/docker
  securityContext:
    runAsUser: 1000
  volumes:
  - name: docker-graph-storage
    emptyDir: {}
"""
        }
    }
    stages {
        stage('Prepare') {
            steps {
                script {
                    commit = sh(returnStdout: true, script: 'git describe --always').trim()
                }
                script {
                    version = readMavenPom().getVersion()
                }
            }
        }
        stage('Test') {
            steps {
                container('jdk') {
                    sh "./mvnw test package"
                }
            }
        }
// TEST BLOCK - CLEAN UP BEFORE PR
        stage('Validate jenkinsfile changes') {
            when {
                branch "jenkins-dind-fix"
            }
            steps {
                container('docker') {
                    withCredentials([usernamePassword(credentialsId:'OvertureDockerHub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh 'docker login -u $USERNAME -p $PASSWORD'
                    }
                    sh "docker build --network=host -f Dockerfile . -t ${dockerHubRepo}:edge -t ${dockerHubRepo}:${commit}"
//                    sh "docker push ${dockerHubRepo}:edge"
                    sh "docker push ${dockerHubRepo}:${commit}"
                }
                container('docker') {
                    withCredentials([usernamePassword(credentialsId:'OvertureBioGithub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh "docker login ${gitHubRegistry} -u $USERNAME -p $PASSWORD"
                    }
                    sh "docker build --network=host -f Dockerfile . -t ${gitHubRegistry}/${gitHubRepo}:edge -t ${gitHubRegistry}/${gitHubRepo}:${commit}"
//                    sh "docker push ${gitHubRegistry}/${gitHubRepo}:edge"
                    sh "docker push ${gitHubRegistry}/${gitHubRepo}:${commit}"
                }

            }
        }
// END OF TEST BLOCK
        stage('Build & Publish Develop') {
            when {
                branch "develop"
            }
            steps {
                container('docker') {
                    withCredentials([usernamePassword(credentialsId:'OvertureDockerHub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh 'docker login -u $USERNAME -p $PASSWORD'
                    }
                    sh "docker build --network=host -f Dockerfile . -t ${dockerHubRepo}:edge -t ${dockerHubRepo}:${commit}"
                    sh "docker push ${dockerHubRepo}:edge"
                    sh "docker push ${dockerHubRepo}:${commit}"
                }
                container('docker') {
                    withCredentials([usernamePassword(credentialsId:'OvertureBioGithub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh "docker login ${gitHubRegistry} -u $USERNAME -p $PASSWORD"
                    }
                    sh "docker build --network=host -f Dockerfile . -t ${gitHubRegistry}/${gitHubRepo}:edge -t ${gitHubRegistry}/${gitHubRepo}:${commit}"
                    sh "docker push ${gitHubRegistry}/${gitHubRepo}:edge"
                    sh "docker push ${gitHubRegistry}/${gitHubRepo}:${commit}"
                }

            }
        }
        stage('Release & tag') {
          when {
            branch "master"
          }
          steps {
                container('docker') {
                    withCredentials([usernamePassword(credentialsId: 'OvertureBioGithub', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                        sh "git tag ${version}"
                        sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${gitHubRepo} --tags"
                    }
                    withCredentials([usernamePassword(credentialsId:'OvertureDockerHub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh 'docker login -u $USERNAME -p $PASSWORD'
                    }
                    sh "docker build --network=host -f Dockerfile . -t ${dockerHubRepo}:latest -t ${dockerHubRepo}:${version}"
                    sh "docker push ${dockerHubRepo}:${version}"
                    sh "docker push ${dockerHubRepo}:latest"
                }
                container('docker') {
                    withCredentials([usernamePassword(credentialsId:'OvertureBioGithub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh "docker login ${gitHubRegistry} -u $USERNAME -p $PASSWORD"
                    }
                    sh "docker build --network=host -f Dockerfile . -t ${gitHubRegistry}/${gitHubRepo}:latest -t ${gitHubRegistry}/${gitHubRepo}:${version}"
                    sh "docker push ${gitHubRegistry}/${gitHubRepo}:${version}"
                    sh "docker push ${gitHubRegistry}/${gitHubRepo}:latest"
                }

          }
        }
    }
}
