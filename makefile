usage:
	@echo Usage:
	@echo "  make build"
	@echo "  make clean"
	@echo "  make run"
	@echo "  make test"

build:
	mvn compile

run:
	mvn exec:java $(JAVA_OPTS) -Dexec.mainClass=JettyLauncher

test:
	mvn test

.PHONY: build clean run test usage
