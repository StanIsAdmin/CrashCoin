#!/bin/bash

if test -t 1; then
    # see if it supports colors...
    ncolors=$(tput colors)

    if test -n "$ncolors" && test $ncolors -ge 8; then
        bold="$(tput bold)"
        underline="$(tput smul)"
        standout="$(tput smso)"
        italic="$(tput sitm)"
        normal="$(tput sgr0)"
        black="$(tput setaf 0)"
        red="$(tput setaf 1)"
        green="$(tput setaf 2)"
        yellow="$(tput setaf 3)"
        blue="$(tput setaf 4)"
        magenta="$(tput setaf 5)"
        cyan="$(tput setaf 6)"
        white="$(tput setaf 7)"
    fi
fi

start() {
    if [ $(status $1) = true ]; then
        echo "${red}${1^} already start${normal}";
    else
        echo "${green}Starting${normal} ${bold}${1^}${normal}";
        screen -S $1 -d -m java -jar dist/$1.jar
    fi
}

stop() {
    if [ $(status $1) = true ]; then
        echo "Stopping ${bold}${1^}${normal}";
        screen -X -S $1 stuff "^C"
        sleep 3
        if [ $(status $1) = true ]; then
            echo "${red}Error when stopping${normal} ${bold}${1^}${normal}"
        else
            echo "${bold}${1^}${normal} is stop"
        fi
    else
        echo "${bold}${1^}${normal} ${red}is not start${normal}";
    fi
}

view() {
    screen -r $1
}

status() {
    if screen -list | grep -q "$1"; then
        echo true
    else
        echo false
    fi
}

printStatus() {
    if screen -list | grep -q "$1"; then
        echo "${green}[V] ${normal}Service ${bold}${1^}${normal} is ${green}running${normal}"
    else
        echo "${red}[X] ${normal}Service ${bold}${1^}${normal} ${red}down${normal}"
    fi
}

startType() {
    cmdSelctType "start" $1
}

stopType() {
    cmdSelctType "stop" $1
}

restartType() {
    cmdSelctType "stop" $1
    sleep 1
    cmdSelctType "start" $1
}

viewType() {
    cmdSelctType "view" $1
}

cmdSelctType() {
    case "$2" in
        all)
            $1 "master"
            sleep 1
            $1 "relay"
            sleep 1
            $1 "client"
            sleep 1
            $1 "miner"
            ;;
        relay)
            $1 "relay"
            ;;
        client)
            $1 "client"
            ;;
        master)
            $1 "master"
            ;;
        miner)
            $1 "miner"
            ;;
       *)
           echo "${red}Usage${normal}: $0 $1 {${italic}all|client|master|relay|miner${normal}}"
           exit 1
           ;;
    esac
}



#Start-Stop here
case "$1" in
    start)
        startType $2
        ;;
    stop)
        stopType $2
        ;;
    restart)
        restartType $2
        ;;
    view)
        echo "To close view 'CTRL+a' and then 'd'"
        viewType $2
        ;;
    status)
        printStatus "master"
        printStatus "relay"
        printStatus "client"
        printStatus "miner"
        ;;
   *)
       echo "${red}Usage${normal}: $0 {${italic}restart|start [type]|stop [type]|status [type]${normal}}"
       exit 1
       ;;
esac
 
exit 0