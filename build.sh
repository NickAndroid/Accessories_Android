./code_stats.sh
./gradlew jarRelease
./gradlew bintrayUpload | tee build/upload.log
 cp -r core/build/libs/ ./download/
