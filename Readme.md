## Summary

This is a REST-full application elaborated via Spring Boot. The application has the following features:

* authentication
* performing CRUD operations with users, files, and events
* uploading files to AWS S3 bucket

The built-in admin is: 

* username: `admin`
* password: `test`

## Prerequisites
1. The IAM user with in AWS cloud. Required permissions: uploading and editing files in S3 buckets
2. S3 bucket is created
3. MySQL database is created
4. Docker is installed (required for creating docker image with the application)

## Building and running the application
1. Pull the repository
2. In the project directory create and fill in the text file `app-env-vars` with the environment variables:
   ~~~
   DB_HOST=
   DB_PORT=
   DB_NAME=
   DB_USERNAME=
   DB_PASSWORD=
   AWS_ACCESS_KEY_ID=
   AWS_SECRET_ACCESS_KEY=
   AWS_S3_REGION=
   AWS_S3_BUCKET_NAME=
   ~~~
   
3. Build docker image with the `file-share-service` name:
   ~~~ shell
   docker build -t file-share-service:1 .
   ~~~

4. Run docker container (example):
   ~~~ shell
   docker run -it -p 8080:8080 --env-file app-env-vars file-share-service:1
   ~~~

5. Tests can be run via the command:
   ~~~
   ./gradlew test
   ~~~
   NOTE: environment variables with fake values should be specified first - AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_S3_REGION, AWS_S3_BUCKET_NAME  