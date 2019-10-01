# 1. How long did you spend on the coding test?
_What would you add to your solution if you spent more time on it?_ 
_If you didn't spend much time on the coding test then use this as an opportunity to explain what you would add._

## Part one

I approached this in 2 stages:

1. I coded up the API using Spring MVC, as this is my daily driver framework. This took about **2 hours** to complete 
satisfactorily with a local deployment using a localhost redis server. This gave me a feeling for how the API should 
work without worrying about new frameworks or tools. This is saved as a separate GitHub project, 
[much-better-boot](https://github.com/cowinr/much-better-boot).
1. I then read up on Ratpack and re-implemented the API. Fortunately I found this could be incorporated into the
existing Spring Boot app fairly well. This took about **4 hours**, again to get it to the stage of a local deployment.
This is saved in this GitHub project, [much-better-ratpack](https://github.com/cowinr/much-better-ratpack).

## Part two

I was pleased to see a good bit of overlap between the way Azure and GCP work which meant that although the Google Cloud 
is new to me, most of the concepts translated from Azure reasonably well.

I followed a [GCP Spring Boot on K8S](https://codelabs.developers.google.com/codelabs/cloud-springboot-kubernetes) doc to deploy 
the app to Kubernetes, including use of the `jib-maven-plugin` to handle creating the container image and pushing it to 
the container registry.

I also had to enable IP aliasing for the Kubernetes cluster to enable it to connect to a Redis server.
This took about **5 hours**.
 
All was built using `gcloud` cloud shell, as below: 

```shell script
# Create redis server in same region / zone
# Read it's IP from the dashboard, and configure in `application.properties`
gcloud redis instances create rc-redis --size=5 --region=us-central1 \
    --zone=us-central1-c --redis-version=redis_4_0

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
much-better-ratpack   LoadBalancer   10.0.15.247   35.188.216.12    5050:30978/TCP   27s
 
# Go to LoadBalancer EXTERNAL-IP
http://35.188.216.12:5050
 
# Scale up
kubectl scale deployment much-better-ratpack --replicas=3
 
kubectl get deployment
 
# Provide an update if needed
kubectl set image deployment/much-better-ratpack \
  much-better-ratpack=gcr.io/$GOOGLE_CLOUD_PROJECT/much-better-ratpack:v2
```

Final part is "expose it via a publicly accessible ingress at https://test.rc.api.mirlimiteduk.com". To do this I 
attempted to use the dashboard GUI -- `Kubernetes Engine -> Services & Ingress`. Unfortunately I've run out of time to
complete this. Currently the API is only available via HTTP at http://35.188.216.12:5050

Final set of tech used:

| Technology | Used before |
|---|---|
| Java 8 | Y |
| Spring Boot | Y |
| Redis, with Spring Data Redis | N |
| Hibernate validator | Y |
| Ratpack | N |
| JUnit / Mockito | Y |
| Lombok | Y |
| Google Cloud | N |
| K8s / Docker | N |
| GKE / Redis in GCP | N |

## What else to add?

I'd take a look whether the following were useful:
- [Redis' reactive support in Spring Data](https://docs.spring.io/spring-data/redis/docs/2.1.10.RELEASE/reference/html/#redis:reactive).
- Adding logging and metrics for debug support, troubleshooting and production monitoring
- Load testing
- Further unit and integration tests, especially to test the "non-happy" paths
- More robust monetary amounts handling - maybe use something like [moneta](https://javamoney.github.io/ri.html)
- Finish up adding the ingress to GKE

# 2. What was the most useful feature that was added to Java 8?
_Please include a snippet of code that shows how you've used it._

Probably functional interfaces and lambda expressions. I note they are used heavily in Ratpack, e.g.:

```java
.get("ping-redis", ctx -> ctx
    .render("Redis user count: " + repository.count()))
```

leading to some very concise code.

Removing boilerplate code like this is appealing as it can lead to more readable code. More readable code is, by definition,
more easily maintainable code.
I also regularly use `lombok` and have previously used `kotlin` for this same reason.

# 3. What is your favourite framework / library / package that you love but couldn't use in the task?
_What do you like about it so much?_

From a perspective of familiarity I would like to have simply stuck with Spring MVC in the Boot app.
This is my daily-driver framework and is where I'm most productive.

I found writing tests for Ratpack much harder than the Spring MVC handlers.
It's interesting to see just how much convenience is brought to the table using the full Spring Framework.

# 4. What great new thing you learnt about in the past year and what are you looking forward to learn more about over the next year?

I've learned a huge amount about the Azure PaaS services over the past 12-18 months. From the network
and Identity & Access Management side, through the full web stack, secure storage, secrets management, API management,
SQL Server, Analysis Services, logging and metrics to the Azure DevOps CI/CD tooling. 

I have a number of tools and technologies on my to-learn list, including:
- Docker & Kubernetes
- Data Science with Python (+ ML, AI etc.)
- Reactive programming, e.g. Spring Web Flux, Reactive JDBC
- Moving from Java 8 -> 11+
- Currently also studying for the AZ-203 Azure Developer certification

Plus I found working with Ratpack quite interesting, so may round out my learning here, and bring in Guice as an
alternative to Boot. 

# 5. How would you track down a performance issue in production? 
_Have you ever had to do this? Can you add anything to your implementation to help with this?_

This is a regular task at work as we maintain several dozen systems, some of which are over 20 years old.

There is no one way to approach a performance issue, as it is rare for them to be alike.
Some techniques used include:

- gather any output, related logging and metrics to get a grip on exactly what the issue is
- if the issue is severe enough gather together one or more experts in the affected systems
- often it is useful to make an announcement to the full IT team in case some other activity is to blame (patching, upgrades, backups etc.)
- logically trace through the full process showing the performance issue, usually from the front-end to the back-end
- at each step of the process look at the behaviour and compare it (if possible) to 'normal' operation
- if any step looks to be behaving suspiciously see what can be done to illuminate the issue further, e.g. increase the logging level
- do whatever it takes to gain sufficient confidence that you have located the root cause - usually by asking a colleague to check your reasoning
- if the fix needs to be made directly in production, and especially if it is manual in nature, make sure there are 2 pairs of eyes watching

The last step is probably the most important... Ask: "How can we prevent this happening again?".

The best ways of helping track issues with systems include:

- Ensure monitoring is in place, even if it is usually 'off'
- Document normal operating parameters
- Document what has already gone wrong with the system so it can be referred to next time
- Write a 'Support' page in the system's wiki detailing everything necessary for analysing potential issues, e.g.
   - Where does the source code live
   - Which database and schema is being used
   - Where to go to get access to the servers
   - Who are the experts to ask
   - What to do in an emergency
   - How to enable extra logging / metrics / monitoring

# 6. How would you improve the APIs that you just used?

I added a few endpoints just to help with testing/confirming the deployment:

- `/` can be used as a health check endpoint, and has the advantage of being callable via a browser.
- `/ping` allows a simple GET request without needing to set an `Authorization` header. I usually add somethin similar 
to APIs to help with connection troubleshooting and as an availability test endpoint from a monitoring system.
- `/ping-redis` is similar but also connects to the Redis server (simply counts the number of users created so far).

## Authentication and User Management

The authentication and authorization part of the API is obviously fairly basic. I'd look to add support for oAuth or
OpenID or similar.

## Handling Foreign Exchange

Other than handling monetary amounts properly, with associated rounding rules, it would seem useful to add an element
of currency exchange to allow spending in currencies other than your current balance. I'd look around for a web service
that provides the data for this and see how the moneta reference implementation of JSR-354 fairs.

A top up for the balance would be another obvious area for adding to the API. Although this could just be a negative
`/spend` transaction.

## Accounting

At some point there needs to be consideration of proper accounting, including reporting. On reflection though this 
would probably not form part of this API.

# 7. Please describe yourself in JSON format.

```json
{
  "name": "richard",
  "facets": [
    "always learning",
    "enjoys a technical challenge",
    "excellent communicator",
    "reliable",
    "all-round supervisory and technical expertise",
    "loves diversity",
    "values team collaboration",
    "proven ability to ensure the smooth running of IT systems",
    "confident presenting to Executive and Clients alike",
    "pragmatic",
    "excellent time management"
  ],
  "non-work-activities": [
    {
      "type": "diving",
      "level": "BSAC Ocean diver",
      "next": "Training for BSAC Sports diver certification"
    },
    {
      "type": "running",
      "level": "5km",
      "next": "Training for 10km www.mo-running.com/belfast"
    },
    {
      "type": "language",
      "level": "Basic Dutch",
      "next": "One-to-one via iTalki"
    },
    {
      "type": "charity",
      "level": "Youth organiser for Manx Diabetic Group",
      "next": "Gorge walking / Coasteering"
    }
  ]
}
```

# 8. What is the meaning of life?

Found a quote that fits:

"The man who is born with a talent which he is meant to use, finds his greatest happiness in using it."
―Goethe