#!/bin/bash

# Function to find and kill a process by port
kill_process_by_port() {
    
    local port=$1
    # Get the process ID (PID) using netstat and awk
    local pid=$(sudo netstat -tpln | grep ":$port " | awk '{print $7}' | awk -F'/' '{print $1}')

    if [ -n "$pid" ]; then
        echo "Process found with PID: $pid"
        kill "$pid"
        echo "Process killed."
    fi
}

kill_process_by_port $1