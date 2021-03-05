# import database functions
source ./fetch_db.sh

# fetch database info
fetch_db_info

# login mysql root user and drop user db_user
sudo mysql -u $mysql_root_user -p$mysql_root_pass -Bse "DROP USER '$db_user'@'$db_adrss';"

# login mysql root user and drop database db_name
sudo mysql -u $mysql_root_user -p$mysql_root_pass -Bse "DROP DATABASE $db_name;"

# delete python virtual environment
rm -r "./env"

