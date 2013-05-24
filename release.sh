#!/bin/sh
echo ""
echo "######################################"
echo "  Releasing Overlord Commons"
echo "######################################"
echo ""
mvn -e --batch-mode clean release:prepare release:perform

