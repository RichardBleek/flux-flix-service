### Flux flix service

After having seen Josh Long his talk at Spring One Amsterdam 2019 I was intrested in trying to recreate what I saw in his talk.
This is the results it should be very similar to the code programmed live by Josh during the talk. Props to him for doing that live!

I added a in memory mongodb, if you are running mongodb locally you can remove it.
All the classes are coded in one big java file, please forgive me, split it up if you actually want to use it for something.

#### build & start
mvn clean package
mvn spring-boot:run

#### endpoints
```
curl localhost:8080/movies
curl localhost:8080/movies/{id}
curl localhost:8080/movies/{id}/events
```

### Event streaming
the third endpoint does event streaming which is the interesting bit.
