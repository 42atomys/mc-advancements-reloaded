GRADLE_VERSION = 8.8

# To fix any import error with net.minecraft.* package, run the following command
# gradle build --refresh-dependencies
build: update
	gradle build 
run:
	gradle run
clean:
	@./gradlew clean
	rm -rf ~/.gradle
	rm -rf .gradle run remappedSrc

hard-clean: clean
	@./gradlew build --refresh-dependencies
	@./gradlew genSources

test:
	gradle test
package:
	gradle package

update:
	bash ./update.sh

upgrade:
	bash ./upgrade_gradle_properties_to_latest.sh

gen-minecraft-source:
	@echo "Generate Minecraft source code"
	@./gradlew genSources

install-vscode:
	@echo "Install Visual Studio Code"
	@./gradlew vscode

.PHONY: build run clean test package
