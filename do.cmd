@echo off

if [%1] == [help] (
  echo  fixgit  set executable flag on git index for required files
  echo  t       test using embedded HSQLDB
  echo  ut      test: run unit tests only, no integration tests
  echo  tc      test and generate coverage data
  echo  sc      compute and upload Sonar analysis to SonarCloud
  echo  tsc     similar to "do tc" then "do sc"
  echo  sb      scan with SpotBugs then show GUI
  echo  b       compile
  echo  c       clean
  echo  p       package
  echo  rd      package and run application with dev profile
  echo  w $V    set or upgrade Maven wrapper to version $V
  echo  cv      check plugins and dependencies versions
  echo  uv      update plugins and dependencies versions
  echo  dt      show dependencies tree
  echo  jib     build Docker image to a Docker daemon
  echo  jibtar  build and save Docker image to a tarball
)

if [%1] == [fixgit] (
  git update-index --chmod=+x do
  echo 'do' has now executable flag on git index
  git update-index --chmod=+x mvnw
  echo 'mvnw' has now executable flag on git index
)
if [%1] == [t] (
  mvnw clean verify
)
if [%1] == [ut] (
  mvnw clean test
)
if [%1] == [tc] (
  mvnw clean verify -P coverage
)
if [%1] == [b] (
  mvnw clean compile -DskipTests -T1
)
if [%1] == [c] (
  mvnw clean
)
if [%1] == [p] (
  mvnw clean package -DskipTests -T1
)
if [%1] == [rd] (
  mvnw clean package -DskipTests -T1
  cd target
  java -jar -Xms128m -Xmx512m -Dspring.profiles.active=dev -Dfile.encoding=UTF-8 -Djava.awt.headless=true -XX:CompileThreshold=1500 manon.jar
  cd ..
)
if [%1] == [w] (
  mvn -N io.takari:maven:wrapper -Dmaven=%2
)
if [%1] == [cv] (
  mvnw versions:display-property-updates -U -P coverage,jib,mig,spotbugs
)
if [%1] == [uv] (
  mvnw versions:update-properties -U -P coverage,jib,mig,spotbugs
)
if [%1] == [dt] (
  mvnw dependency:tree
)
if [%1] == [sc] (
  mvnw sonar:sonar -Dsonar.organization=%TK1_MANON_SONAR_ORGA% -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=%TK1_MANON_SONAR_LOGIN%
)
if [%1] == [tsc] (
  mvnw clean verify sonar:sonar -P coverage -Dsonar.organization=%TK1_MANON_SONAR_ORGA% -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=%TK1_MANON_SONAR_LOGIN%
)
if [%1] == [sb] (
  mvnw clean compile spotbugs:spotbugs spotbugs:gui -P spotbugs
)
if [%1] == [jib] (
  mvnw clean compile jib:dockerBuild -DskipTests -P jib
)
if [%1] == [jibtar] (
  mvnw clean compile jib:buildTar -DskipTests -P jib
)
