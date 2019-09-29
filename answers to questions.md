# 1. How long did you spend on the coding test?
_What would you add to your solution if you spent more time on it?_ 
_If you didn't spend much time on the coding test then use this as an opportunity to explain what you would add._

## Part one

I approached this in 2 stages:

1. I coded up the API using Spring MVC, as this is my daily driver framework. This took about **2 hours** to complete 
satisfactorily with a local deployment using a localhost redis server. This gave me a feeling for how the API should 
work without worrying about new frameworks or tools. This is saved as a separate GitHub project, 
[much-better-boot](https://github.com/cowinr/much-better-boot).
2. I then read up on Ratpack and re-implemented the API. Fortunately I found this could be incorporated into the
existing Spring Boot app fairly well. This took about **4 hours**, again to get it to the stage of a local deployment.
This is saved in this GitHub project, [much-better-ratpack](https://github.com/cowinr/much-better-ratpack).

## Part two

I was pleased to see a good bit of overlap between the way Azure and GCP work which meant that although the Google Cloud 
is new to me, most of the concepts translated from Azure reasonably well.

I followed a [GCP Spring Boot on K8S](https://codelabs.developers.google.com/codelabs/cloud-springboot-kubernetes) doc to deploy 
the app to Kubernetes, including use of the `jib-maven-plugin` to handle creating the container image and pushing it to 
the container registry. I had to enable IP aliasing for the Kubernetes cluster to enable it to connect to a Redis server.
This took about **5 hours**.
 
All was built using `gcloud` cloud shell, as below: 

```shell script
git clone https://github.com/cowinr/much-better-ratpack.git
cd much-better-ratpack
 
# optionally run
./mvnw -DskipTests spring-boot:run
 
# Access on 5050
https://5050-dot-9014085-dot-devshell.appspot.com/?authuser=0
 
# Package
./mvnw -DskipTests package
 
# Use Jib to create the container image and push it to the Container Registry.
./mvnw -DskipTests com.google.cloud.tools:jib-maven-plugin:build \
  -Dimage=gcr.io/$GOOGLE_CLOUD_PROJECT/much-better-ratpack:v1
 
# test the image locally - run a Docker container as a daemon on port 8080 from newly-created container image
docker run -ti --rm -p 5050:5050 \
  gcr.io/$GOOGLE_CLOUD_PROJECT/much-better-ratpack:v1
 
# Create cluster (with IP alias enabled)
gcloud container clusters create much-better-ratpack-cluster \
  --num-nodes 2 \
  --machine-type n1-standard-1 \
  --zone us-central1-c \
  --enable-ip-alias
 
# Deploy to cluster
kubectl run much-better-ratpack \
  --image=gcr.io/$GOOGLE_CLOUD_PROJECT/much-better-ratpack:v1 \
  --port=5050
 
# View deployments
kubectl get deployments
 
# View app instances
kubectl get pods
 
# Allow external traffic
kubectl expose deployment much-better-ratpack --type=LoadBalancer
 
# IP address of the service - list all the cluster services
kubectl get services
 
NAME                  TYPE           CLUSTER-IP    EXTERNAL-IP      PORT(S)          AGE
kubernetes            ClusterIP      10.0.0.1      <none>           443/TCP          16m
much-better-ratpack   LoadBalancer   10.0.15.247   104.197.224.84   5050:30978/TCP   27s
 
# Go to LoadBalancer EXTERNAL-IP
http://104.197.224.84:5050
 
# Scale up
kubectl scale deployment much-better-ratpack --replicas=3
 
kubectl get deployment
 
# Provide an update
kubectl set image deployment/much-better-ratpack \
  much-better-ratpack=gcr.io/$GOOGLE_CLOUD_PROJECT/much-better-ratpack:v2

# Create redis server in same region / zone
gcloud redis instances create rc-redis --size=5 --region=us-central1 \
    --zone=us-central1-c --redis-version=redis_4_0
```

## What else to add?

I'd take a look whether the following were useful:
- [Redis' reactive support in Spring Data](https://docs.spring.io/spring-data/redis/docs/2.1.10.RELEASE/reference/html/#redis:reactive).
- Adding logging and metrics for debug support, troubleshooting and production monitoring
- Load testing
- Further unit and integration tests, especially to test the "non-happy" paths.

# 2. What was the most useful feature that was added to Java 8?
_Please include a snippet of code that shows how you've used it._

# 3. What is your favourite framework / library / package that you love but couldn't use in the task?
_What do you like about it so much?_

From a perspective of familiarity I would like to have simply stuck with Spring MVC in the Boot app. This is my 
daily-driver framework, so is where I'm most productive.

# 4. What great new thing you learnt about in the past year and what are you looking forward to learn more about over the next year?

I've learned a huge amount about the Azure PaaS services over the past 12-18 months. This includes everything from the network
and Identity & Access Management side, through the full web stack, secure storage, secrets management, API management,
logging and metrics to the Azure DevOps CI/CD tooling. I've also developed a set of standards for usage, and given a number of
presentations to the other developers to get them up-to-speed.

I have a number of tools and technologies on my to-do list, including:
- Docker & Kubernetes
- Data Science with Python

# 5. How would you track down a performance issue in production? 
_Have you ever had to do this? Can you add anything to your implementation to help with this?_

# 6. How would you improve the APIs that you just used?

I added a couple of endpoints just to help with testing/confirming the deployment:
- `/ping` allows a simple GET request without needing to set an `Authorization` header. I usually add somethin similar 
to APIs to help with connection troubleshooting and as an availability test endpoint from a monitoring system.
- `/ping-redis` is similar but also connects to the Redis server (simply counts the number of users created so far).

## Authentication and User Management


## Handling Foreign Exchange


# 7. Please describe yourself in JSON format.

# 8. What is the meaning of life?
