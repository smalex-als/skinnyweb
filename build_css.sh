#!/bin/sh

echo "Building css.."

JAVA_HOME=`/usr/libexec/java_home -v 1.8`

DST=target/skinnyweb-1.0/css/mini.css

java -jar bin/closure-stylesheets.jar \
  --allow-unrecognized-properties \
  --allow-unrecognized-functions \
  src/main/webapp/css/clear.css \
  src/main/webapp/css/pure-buttons.css \
  src/main/webapp/css/menus-core.css \
  src/main/webapp/css/menus-horizontal.css \
  src/main/webapp/css/menus-skin.css \
  src/main/webapp/css/main.css \
  src/main/webapp/css/font-awesome.css \
  > "$DST"

wc "$DST"

# --pretty-print \
# --output-renaming-map-format CLOSURE_UNCOMPILED \
# --rename CLOSURE \
# --output-renaming-map renaming_map.js \
