#!/bin/bash

# VARIABLES
MINER_WALLET_USERNAME="test"
MINER_WALLET_PASSWORD="test"

SCREEN_CONFIG="~/.screenrc"


command -v screen >/dev/null 2>&1 || { echo >&2 "Screen is not installed. Install screen by typing 'apt-get install screen' in order to use Crashcoin."; exit 1; }

if [ ! -f "$SCREEN_CONFIG" ]; then
    echo "Do you want to create file $SCREEN_CONFIG to allow scroll on screen [Y,n]"
    read input
    if [[ $input == "Y" || $input == "y" ]]; then
        echo "termcapinfo xterm* ti@:te@" >> ~/.screenrc # Could not define variable
    fi
fi


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
        echo "${red}${1^} already started${normal}";
    else
        echo "${green}Starting${normal} ${bold}${1^}${normal}";
        screen -S $1 -d -m java -jar dist/$1.jar $2
    fi
}

stop() {
    if [ $(status $1) = true ]; then
        echo "Stopping ${bold}${1^}${normal}";
        screen -X -S $1 stuff "^C"
        sleep 2
        if [ $(status $1) = true ]; then
            echo "${red}Error while trying to${normal} ${bold}${1^}${normal}"
        else
            echo "${bold}${1^}${normal} has been stopped"
        fi
    else
        echo "${bold}${1^}${normal} ${red}has not been launched yet${normal}";
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
        echo "${red}[X] ${normal}Service ${bold}${1^}${normal} is ${red}down${normal}"
    fi
}

startType() {
    cmdSelectType "start" $1
}

stopType() {
    cmdSelectType "stop" $1
}

restartType() {
    cmdSelectType "stop" $1
    sleep 1
    cmdSelectType "start" $1
}

viewType() {
    cmdSelectType "view" $1
}

cmdSelectType() {
    case "$2" in
        all)
            $1 "master"
            sleep 1
            $1 "relay"
            sleep 1
            $1 "client"
            sleep 1
            $1 "miner" "$MINER_WALLET_USERNAME $MINER_WALLET_PASSWORD"
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
            $1 "miner" "$MINER_WALLET_USERNAME $MINER_WALLET_PASSWORD"
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
        echo "To close view : press 'CTRL+a' and then 'd'"
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
