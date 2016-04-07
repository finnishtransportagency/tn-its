usage:
	@echo Usage:
	@echo "  make build   — Build project"
	@echo "  make clean   — Remove build files"
	@echo "  make run     — Run TN-ITS API server"
	@echo "  make convert — Run Rosatte converter"
	@echo "  make test    — Run tests"

build:
	mvn compile

run: build
	mvn exec:java $(JAVA_OPTS) -Dexec.mainClass=JettyLauncher

convert: build
	mvn exec:java $(JAVA_OPTS) -Dexec.mainClass=fi.liikennevirasto.digiroad2.tnits.rosatte.RosatteConverter

test: build
	mvn test

clean:
	mvn clean

.PHONY: build clean run test usage convert
