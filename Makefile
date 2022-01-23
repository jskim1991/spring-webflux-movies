info-service:
	cd movies-info-service && mvn verify

review-service:
	cd movies-review-service && mvn verify

movies-service:
	cd movies-service && mvn verify

all:
	make info-service
	make review-service
	make movies-service