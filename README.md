# protect-PM-Lee-frontend
Learning to be good Info-Sec students to protect PM Lee's data

## Cygwin

### Run front-end server
```bash
yarn start
```

## Developer Guide

For deployment on local host (perhaps, for testing purposes), go to `local-deployment` branch. This branch uses H2 database (instead of MySQL) which is in-built and doesn't require additional configuration, whereas MySQL database requires an additional step of you setting up a local MySQL database. 

You can simply cherry-pick this commit on top of the branch that you are working on to change the database that our application uses from MySQL to H2.

### Frontend
To run the front-end, go to 'healthcare-app-client' and run:
1. `yarn install`
1. `yarn start`

### Backend
To run the server, go to the backend directory and run `gradle bootRun` (or `./gradlew bootRun` for Linux).
