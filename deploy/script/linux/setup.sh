#!/bin/bash

installUtils()
{
	apt update;
	apt install openjdk-11-jdk
	apt install net-tools;
}

addUserWithNetstatPermission() {
	local lusername=$1
	local netstat_path=$(which netstat)
	echo "$lusername ALL=NOPASSWD: $netstat_path" >> /etc/sudoers
	visudo -c
	echo "User $lusername added with permission to execute netstat."
}

createDesktopShortcut()
{
	local current_dir=$(pwd)
	local user_home="/home/$1"

	echo -e "[Desktop Entry]\nName=eCScribe\nComment=eCScribe Application\nExec=$current_dir/start.sh\nIcon=$current_dir/icon.png\nTerminal=false\nType=Application" > eCScribe.desktop
	cp eCScribe.desktop $user_home/.local/share/applications/
	chown $1:$1 $user_home/.local/share/applications/eCScribe.desktop
	chmod +x $user_home/.local/share/applications/eCScribe.desktop

	cp eCScribe.desktop $user_home/Desktop/
	chown $1:$1 $user_home/Desktop/eCScribe.desktop
	chmod +x $user_home/Desktop/eCScribe.desktop
	echo "Created desktop shortcut"
}

modifyDcm4chePermission()
{
	local current_dir=$(pwd)
	local dcm4che_folder="$current_dir/dcm4che"
	chown $1:$1 -R $dcm4che_folder
	chmod +x -R $dcm4che_folder/bin/*
	echo "Modified the permissions"

}

if [ "$EUID" -ne 0 ]; then
    echo "Please run this script as root."
    exit 1
fi

installUtils

read -p "Enter the username to add to sudoers: " username
if ! id "$username" &>/dev/null; then
    echo "User $username does not exist."
    exit 1
fi

createDesktopShortcut $username
modifyDcm4chePermission $username
addUserWithNetstatPermission $username