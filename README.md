# VuSpeech Project

Welcome to the VuSpeech project. Please follow the instructions below to try it.

## Instructions for a local machine
It is assumed that you are using a Ubuntu machine. The following steps have been tested in a Ubuntu 18.04 LTS machine.
- **Database root user configuration:** If you do not have MySQL installed, please go to the next step. Otherwise, please make sure that your MySQL root user credential is correctly written in `db_root_credential.json` file. The MySQL root user will be used to create an additional user account and a database for the project.
- **Run setup:** Run `setup.sh` to set up the environment for running the Django server with MySQL in a local machine. You will need to enter your Ubuntu user account password to install prerequisites.
- **Run server:** Run `run_server.sh` to run the Django server in the local machine after initial setup.
- **Visit server:** Visit the web link shown in the output of `run_server.sh` to test if the server is configured properly.
- **Quit server:** To quit the server, press **Ctrl + C** on the terminal.
- **Uninstall database:** If you want to remove the user account and the database for the project from MySQL, please run `uninstall_db.sh`. If you have other MySQL user accounts and/or databases for other projects, they will not be removed.
