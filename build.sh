./code_stats.sh
./gradlew clean
./gradlew jarRelease
rm -rf ./downloads
mkdir downloads
cp -r accessories/build/libs/* ./downloads/
