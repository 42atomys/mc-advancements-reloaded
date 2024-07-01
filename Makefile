GRADLE_VERSION = 8.8

# To fix any import error with net.minecraft.* package, run the following command
# gradle build --refresh-dependencies
build: update
	gradle build 
run:
	gradle run
clean:
	gradle clean
	rm -rf ~/.gradle
	rm -rf .gradle run remappedSrc
test:
	gradle test
package:
	gradle package

update:
	bash ./update.sh

upgrade:
	bash ./upgrade_gradle_properties_to_latest.sh

gradle-update:
	@echo "Update gradle wrapper to version $(GRADLE_VERSION)"
	@bash ./upgrade_gradle.sh $(GRADLE_VERSION)

gen-minecraft-source:
	@echo "Generate Minecraft source code"
	@./gradlew genSources

install-vscode:
	@echo "Install Visual Studio Code"
	@./gradlew vscode

.PHONY: build run clean test package
