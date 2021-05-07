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

## Instructions for server deployment
It is assumed that you are using Amazon AWS for server deployment.
- Use Amazon EC2 to create an instance of Amazon Linux or Ubuntu Server platform.
- Under the EC2 instance, make sure that Python3 has been installed properly.
- Clone the VuSpeech repository (or extract from the ZIP file of the repository).
- Change the credentials and addresses of the database in  `vu-speech/vu_speech/vu_speech/database.json` file according to the database created with Amazon RDS.
- The `setup.sh` script is for initializing the environment. You might need to do minor modifications in the script according to your environment. For example, if you are using CentOS, you need to replace apt-get with yum in the script. Also, for the database hosted on AWS RDS, the database creation commands need to be omitted from the script. After the appropriate modifications in the script, run the script to initialize the environment.
- Put the AWS domain address (either Public IPv4 DNS or address or both) in the `ALLOWED_HOSTS` array defined in the `vu_speech/vu_speech/settings.py` file.
- Run run_server.sh script to ensure that the server is properly set up.
- Terminate the server by pressing Control-C.
- Create a superuser for the Django server. Follow [this](https://docs.djangoproject.com/en/3.2/intro/tutorial02/#creating-an-admin-user).
- Run `async_run_server.sh` script to run the server asynchronously.
- You are done!

Also, keep in mind that the above instructions are just for trying out the service but not using it in a production environment. For using it in a production environment, more security measures need to be taken. For example:
- Remove the `SECRET_KEY` from `vu_speech/vu_speech/settings.py` file. Instead, use a secret key as an OS environment variable.
- Make `DEBUG` to `False` in `vu_speech/vu_speech/settings.py` file.
- Use a dedicated server (such as Apache, Nginx) for hosting static files.
- Obtain a valid X.509 certificate (not self-signed) from a trusted CA and integrate it with the server to ensure a secure HTTPS connection.
- Use a strong password for the database.
- Use all credentials in the form of OS environment variables instead of using them in the scripts (like `vu_speech/vu_speech/database.json`).
