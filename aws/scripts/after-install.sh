#!/bin/bash
set -xe


# Copy war file from S3 bucket to tomcat webapp folder
aws s3 cp s3://codedeploystack-webappdeploymentbucket-pig9d1atoqqu/cache-service-1.0-SNAPSHOT.jar /usr/local/tomcat9/webapps/cache-service-1.0-SNAPSHOT.jar


# Ensure the ownership permissions are correct.
chown -R tomcat:tomcat /usr/local/tomcat9/webapps