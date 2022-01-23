Reactive




Anytime dealing with transformation that is going to return you a reactive type, `flatmap` is used

## Merging different objects into DTO
zip vs flatmap: `flatmap` is preferred when the condition of the first and second API are different?
```
Mono<MovieInfo> movieInfoMono = movieInfoService.retrieveMovieInfoMonoUsingId(movieId);
Mono<List<Review>> reviewList = reviewService.retrieveReviewsFlux(movieId)
        .collectList();
 return movieInfoMono.zipWith(reviewList, (movieInfo, reviews) -> new Movie(movieInfo, reviews));
```

flatmap vs concatMap

```
Mono<MovieInfo> movieInfoMono = movieInfoService.retrieveMovieInfoMonoUsingId(movieId);
return movieInfoMono.flatMap(movieInfo -> {
    Mono<List<Review>> reviewsMono = reviewService.retrieveReviewsFlux(movieInfo.getMovieInfoId
()).collectList();
    return reviewsMono.map(list -> new Movie(movieInfo, list));
});
```












# Sample Log
```
2022-01-15 17:56:33.561 DEBUG 29918 --- [ctor-http-nio-2] r.n.http.server.HttpServerOperations     : [de871419, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] New http connection, requesting read
2022-01-15 17:56:33.562 DEBUG 29918 --- [ctor-http-nio-2] reactor.netty.transport.TransportConfig  : [de871419, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] Initialized pipeline DefaultChannelPipeline{(reactor.left.httpCodec = io.netty.handler.codec.http.HttpServerCodec), (reactor.left.httpTrafficHandler = reactor.netty.http.server.HttpTrafficHandler), (reactor.right.reactiveBridge = reactor.netty.channel.ChannelOperationsHandler)}
2022-01-15 17:56:33.576 DEBUG 29918 --- [ctor-http-nio-2] r.n.http.server.HttpServerOperations     : [de871419, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] Increasing pending responses, now 1
2022-01-15 17:56:33.579 DEBUG 29918 --- [ctor-http-nio-2] reactor.netty.http.server.HttpServer     : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] Handler is being applied: org.springframework.http.server.reactive.ReactorHttpHandlerAdapter@1f3591ce
2022-01-15 17:56:33.586 DEBUG 29918 --- [ctor-http-nio-2] o.s.w.s.adapter.HttpWebHandlerAdapter    : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] HTTP POST "/v1/movieinfos"
2022-01-15 17:56:33.599 DEBUG 29918 --- [ctor-http-nio-2] s.w.r.r.m.a.RequestMappingHandlerMapping : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] Mapped to io.jay.moviesinfoservice.controller.MoviesInfoController#addMovieInfo(MovieInfo)
2022-01-15 17:56:33.610 DEBUG 29918 --- [ctor-http-nio-2] .r.m.a.RequestBodyMethodArgumentResolver : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] Content-Type:application/json
2022-01-15 17:56:33.641 DEBUG 29918 --- [ctor-http-nio-2] .r.m.a.RequestBodyMethodArgumentResolver : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] 0..1 [io.jay.moviesinfoservice.domain.MovieInfo]
2022-01-15 17:56:33.652 DEBUG 29918 --- [ctor-http-nio-2] reactor.netty.channel.FluxReceive        : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] FluxReceive{pending=0, cancelled=false, inboundDone=false, inboundError=null}: subscribing inbound receiver
2022-01-15 17:56:33.668 DEBUG 29918 --- [ctor-http-nio-2] o.s.http.codec.json.Jackson2JsonDecoder  : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] Decoded [MovieInfo(movieInfoId=null, name=Lord of the Ring2s, year=2001, cast=[Aragon, Legolas, Gimli], relea (truncated)...]
2022-01-15 17:56:33.741 DEBUG 29918 --- [ctor-http-nio-2] o.s.w.r.r.m.a.ResponseBodyResultHandler  : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] Using 'application/json' given [*/*] and supported [application/json, application/*+json, application/x-ndjson, text/event-stream]
2022-01-15 17:56:33.741 DEBUG 29918 --- [ctor-http-nio-2] o.s.w.r.r.m.a.ResponseBodyResultHandler  : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] 0..1 [io.jay.moviesinfoservice.domain.MovieInfo]
2022-01-15 17:56:33.747  INFO 29918 --- [ctor-http-nio-2] reactor.Mono.UsingWhen.1                 : onSubscribe(MonoUsingWhen.MonoUsingWhenSubscriber)
2022-01-15 17:56:33.747  INFO 29918 --- [ctor-http-nio-2] reactor.Mono.Log.2                       : onSubscribe(FluxPeek.PeekSubscriber)
2022-01-15 17:56:33.747  INFO 29918 --- [ctor-http-nio-2] reactor.Mono.Log.2                       : request(unbounded)
2022-01-15 17:56:33.747  INFO 29918 --- [ctor-http-nio-2] reactor.Mono.UsingWhen.1                 : request(unbounded)
2022-01-15 17:56:33.756 DEBUG 29918 --- [ctor-http-nio-2] o.s.d.m.core.ReactiveMongoTemplate       : Inserting Document containing fields: [name, year, cast, release_date, _class] in collection: movieInfo
2022-01-15 17:56:33.806  INFO 29918 --- [ntLoopGroup-3-3] org.mongodb.driver.connection            : Opened connection [connectionId{localValue:3, serverValue:460}] to localhost:27017
2022-01-15 17:56:33.843 DEBUG 29918 --- [ntLoopGroup-3-3] org.mongodb.driver.protocol.command      : Sending command '{"insert": "movieInfo", "ordered": true, "txnNumber": 1, "$db": "test", "lsid": {"id": {"$binary": {"base64": "udU9lKRQQQmYoa0DqUlSwg==", "subType": "04"}}}, "documents": [{"_id": {"$oid": "61e28c41cc30ea25ce8a2319"}, "name": "Lord of the Ring2s", "year": 2001, "cast": ["Aragon", "Legolas", "Gimli"], "release_date": {"$date": "2000-12-31T15:00:00Z"}, "_class": "io.jay.moviesinfoservice.domain.MovieInfo"}]}' with request id 5 to database test on connection [connectionId{localValue:3, serverValue:460}] to server localhost:27017
2022-01-15 17:56:33.869 DEBUG 29918 --- [ntLoopGroup-3-3] org.mongodb.driver.protocol.command      : Execution of command with request id 5 completed successfully in 38.78 ms on connection [connectionId{localValue:3, serverValue:460}] to server localhost:27017
2022-01-15 17:56:33.876  INFO 29918 --- [ntLoopGroup-3-3] reactor.Mono.UsingWhen.1                 : onNext(MovieInfo(movieInfoId=61e28c41cc30ea25ce8a2319, name=Lord of the Ring2s, year=2001, cast=[Aragon, Legolas, Gimli], release_date=2001-01-01))
2022-01-15 17:56:33.876  INFO 29918 --- [ntLoopGroup-3-3] reactor.Mono.Log.2                       : onNext(MovieInfo(movieInfoId=61e28c41cc30ea25ce8a2319, name=Lord of the Ring2s, year=2001, cast=[Aragon, Legolas, Gimli], release_date=2001-01-01))
2022-01-15 17:56:33.878 DEBUG 29918 --- [ntLoopGroup-3-3] o.s.http.codec.json.Jackson2JsonEncoder  : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] Encoding [MovieInfo(movieInfoId=61e28c41cc30ea25ce8a2319, name=Lord of the Ring2s, year=2001, cast=[Aragon, Le (truncated)...]
2022-01-15 17:56:33.883  INFO 29918 --- [ntLoopGroup-3-3] reactor.Mono.UsingWhen.1                 : onComplete()
2022-01-15 17:56:33.883  INFO 29918 --- [ntLoopGroup-3-3] reactor.Mono.Log.2                       : onComplete()
2022-01-15 17:56:33.890 DEBUG 29918 --- [ctor-http-nio-2] r.n.http.server.HttpServerOperations     : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] Decreasing pending responses, now 0
2022-01-15 17:56:33.891 DEBUG 29918 --- [ctor-http-nio-2] o.s.w.s.adapter.HttpWebHandlerAdapter    : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] Completed 201 CREATED
2022-01-15 17:56:33.892 DEBUG 29918 --- [ctor-http-nio-2] r.n.http.server.HttpServerOperations     : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] Last HTTP response frame
2022-01-15 17:56:33.892 DEBUG 29918 --- [ctor-http-nio-2] r.n.http.server.HttpServerOperations     : [de871419-1, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:58307] Last HTTP packet was sent, terminating the channel
```