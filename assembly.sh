#!/usr/bin/env bash
rm target/* -r
mvn compile assembly:single
cp target/Game-1.0-jar-with-dependencies.jar .
mkdir server -p
rm server/* -rf
cp Game-1.0-jar-with-dependencies.jar server/.
cp -r server_tml server/.
cp -r public_html server/.
cp -r cfg server/.
cp -r data server/.
cp -r start.sh server/.
tar czf server.tar.gz server
mv server.tar.gz server/.
