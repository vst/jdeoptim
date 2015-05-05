clean:
	mvn clean

compile:
	mvn compile

test:
	mvn test

package:
	mvn package

install:
	mvn install -Dgpg.skip=true

deploy:
	mvn deploy
