node('maven') {
   // define commands
   def mvnCmd = "mvn -s cicd-settings.xml"

   stage ('Build') {
     git branch: 'master', url: 'http://gogs:3000/developer/openshift-tasks.git'
     sh "${mvnCmd} clean install -DskipTests=true"
   }

   stage ('Test and Analysis') {
     parallel (
         'Test': {
             sh "${mvnCmd} test"
             step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
         },
         'Static Analysis': {
             sh "${mvnCmd} jacoco:report sonar:sonar -Dsonar.host.url=http://sonarqube:9000 -DskipTests=true"
         }
     )
   }

   stage ('Push to Nexus') {
    sh "${mvnCmd} deploy -DskipTests=true"
   }

   stage ('Deploy DEV') {
     // clean old build OpenShift
     sh "rm -rf oc-build && mkdir -p oc-build/deployments"
     sh "cp target/openshift-tasks.war oc-build/deployments/ROOT.war"
     // change project to DEV
     sh "oc project dev"
     // clean up. keep the image stream
     sh "oc delete bc,dc,svc,route -l app=tasks -n dev"
     // create build. override the exit code since it complains about exising imagestream
     sh "oc new-build --name=tasks --image-stream=jboss-eap70-openshift --binary=true --labels=app=tasks -n dev || true"
     // build image
     sh "oc start-build tasks --from-dir=oc-build --wait=true -n dev"
     // deploy image
     sh "oc new-app tasks:latest -n dev"
     sh "oc expose svc/tasks -n dev"
   }

   stage ('Deploy STAGE') {
     timeout(time:5, unit:'MINUTES') {
        input message: "Promote to STAGE?", ok: "Promote"
     }

     def v = version()
     // tag for stage
     sh "oc tag dev/tasks:latest stage/tasks:${v}"
     sh "oc project stage"
     // clean up. keep the imagestream
     sh "oc delete bc,dc,svc,route -l app=tasks -n stage"
     // deploy stage image
     sh "oc new-app tasks:${v} -n stage"
     sh "oc expose svc/tasks -n stage"
   }
}

def version() {
  def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
  matcher ? matcher[0][1] : null
}
