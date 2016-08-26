./code_stats.sh
./gradlew clean
./gradlew jarRelease
rm -rf ./downloads
cp -r accessories/build/libs/* ./downloads/
