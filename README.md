# Valamis Learning Paths

**https://valamis.arcusys.com/**

[![build status](https://api.travis-ci.org/arcusys/learning-paths.png)](http://travis-ci.org/arcusys/learning-paths)

### NOTE
Since version 3.4, Valamis Community Edition is separated into three packages on GitHub: Valamis LRS (Learning Record Store, https://github.com/arcusys/valamis-lrs),
Learning Paths (this repository), Valamis components (https://github.com/arcusys/Valamis). You need to compile all of these.

### Building scala code
This is an sbt project.

#### Liferay 6.2
`sbt package`

Deploy to the running Liferay instance
`sbt deploy`

#### Liferay DXP
`sbt osgiCollectDependencies osgiPackage`

Deploy the package and all dependencies to the running Liferay instance

`sbt osgiFullDeploy`

### Assemble js and css
Navigate to web-resources/dev/

`cp .env.example .env`

`npm install`

Change line #18 at web-resources/dev/gulp/config.js

If you use Liferay 6.2, set
`liferayVersion: 6`

If you use Liferay DXP, set
`liferayVersion: 7`

Run `gulp`

`sbt deploy` or `sbt osgiFullDeploy` according of your LR version.


### Known issues
If you have several tomcat instances running, specify liferay home dir in deploy and osgiFullDeploy commands:

`sbt deploy /opt/liferay-portal-6.2-ce-ga6`

`sbt osgiFullDeploy /opt/liferay-dxp-digital-enterprise-7.0-sp4`

If you use deploy and osgiFullDeploy commands you can omit package or osgiPackage commands.

## Version 1.1
Curriculum Manager and Viewer were merged into the Learning Paths portlet. This new logic implies that a learning path is
 a set of learning goals that you achieve in order to get a certificate. Some goals, like lessons, can be completed 
 straight from the UI of the relevant learning path.
