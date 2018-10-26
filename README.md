# protect-PM-Lee
Learning to be good Info-Sec students to protect PM Lee's data

## Developer Guide

### Frontend
To run the front-end, go to 'healthcare-app-client' and run:
1. `yarn install`
1. `yarn start`

To run on VM:
1. `vim package.json`
1. change `start` to `PORT=80 react-app-rewired start`
1. change `proxy` to `https://ifs4205-t1-3.comp.nus.edu.sg`
1. add new file `.env.development.local` with a single line `DANGEROUSLY_DISABLE_HOST_CHECK=true`
1. `yarn start`

### Backend
To run the server, go to the backend directory and run:
1. `gradlew.bat build` (or `./gradlew build` for Mac/Linux).
1. `cp ./build/libs/protect-PM-Lee-frontend-server-0.0.1-SNAPSHOT.jar .`
1. `java -jar protect-PM-Lee-frontend-server-0.0.1-SNAPSHOT.jar --jasypt.encryptor.password="QOa9cJMTJzwN0lPUj3gU"`

To run on VM:
1. `cd src/main/java/org/cs4239/team1/protectPMLeefrontendserver/config/`
1. `vim WebMvcConfig.java`
1. change `http://localhost:3000` to `ifs4205-t1-2.comp.nus.edu.sg`
1. `cd /home/sadm/Downloads/protect-PM-Lee-frontend/backend-server/`
1. `cd src/main/resources/`
1. `vim application.properties`
1. change `server.port` from `5000` to `80`
1. `cd /home/sadm/Downloads/protect-PM-Lee-frontend/backend-server/`
1. `./gradlew build`
1. `cp ./build/libs/protect-PM-Lee-frontend-server-0.0.1-SNAPSHOT.jar .`
1. `java -jar protect-PM-Lee-frontend-server-0.0.1-SNAPSHOT.jar --jasypt.encryptor.password="QOa9cJMTJzwN0lPUj3gU"`
