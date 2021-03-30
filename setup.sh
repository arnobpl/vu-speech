# import database functions
source ./fetch_db.sh

# handle default python version
if [[ "$(python --version 2>&1)" == "Python 2"* ]]; then
  python() {
    python3 "${@}"
  }
fi

# install prerequisites
sudo apt-get install -y jq python3-venv python3-dev mysql-server libmysqlclient-dev  portaudio19-dev python3-pyaudio

# create python virtual environment
python -m venv env

# activate python virtual environment
source env/bin/activate

# install requirements in python virtual environment
pip install -r requirements.txt

# deactivate python virtual environment
deactivate

# fetch database info
fetch_db_info

# login mysql root user and create user db_user
sudo mysql -u $mysql_root_user -p$mysql_root_pass -Bse "CREATE USER '$db_user'@'$db_adrss' IDENTIFIED BY '$db_pass'; GRANT ALL PRIVILEGES ON *.* TO '$db_user'@'$db_adrss' WITH GRANT OPTION; FLUSH PRIVILEGES;"

# login mysql db_user and create db_name
mysql -u $db_user -p$db_pass -Bse "CREATE DATABASE $db_name;"
