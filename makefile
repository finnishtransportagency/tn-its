mvn  := mvn -q -e
PORT := 8090

usage:
	@echo "Usage:"
	@echo "  make build   — Build project"
	@echo "  make clean   — Remove build files"
	@echo "  make run     — Run TN-ITS API server"
	@echo "  make convert — Run Rosatte converter"
	@echo "  make test    — Run tests"
	@echo "  make doc     – Generate API documentation"

build:
	$(mvn) package

run: build
	$(mvn) jetty:run -Djetty.http.port=$(PORT)

convert: build
	$(mvn) exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass=fi.liikennevirasto.digiroad2.tnits.runners.Converter

test: build
	$(mvn) test

clean:
	$(mvn) clean
	
doc:
	$(mvn) scala:doc
	open target/site/scaladocs/index.html	

.PHONY: build clean run test usage convert doc
