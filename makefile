usage:
	@echo Usage:
	@echo "  make build — Build project"
	@echo "  make clean — Remove build files"
	@echo "  make run   — Run server"
	@echo "  make test  — Run tests"

build:
	mvn compile

run:
	mvn exec:java $(JAVA_OPTS) -Dexec.mainClass=JettyLauncher

test:
	mvn test

.PHONY: build clean run test usage
