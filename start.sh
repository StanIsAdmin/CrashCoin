#!/bin/bash

echo "Starting Master";
screen -S master -d -m
screen -S master -X exec java -jar dist/master.jar
sleep 1
echo "Starting Relay";
screen -S relay -d -m
screen -S relay -X exec java -jar dist/relay.jar
sleep 1
echo "Starting Client";
screen -S client -d -m
screen -S client -X exec java -jar dist/client.jar
sleep 1
