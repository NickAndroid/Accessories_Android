./code_stats.sh
./gradlew jarRelease
./gradlew bintrayUpload | tee build/upload.log
 cp -r media.accessories/build/libs/ ./download/
