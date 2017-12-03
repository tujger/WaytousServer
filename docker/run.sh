#!/usr/bin/env bash
cd /home/waytous/WEB-INF/classes
exec java -classpath .:../lib/guava-20.0.jar:../lib/* com.edeqa.waytousserver.WaytousServer /home/waytous/conf/options_docker.json