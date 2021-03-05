# function for fetching database info
fetch_db_info() {
  # fetch database info from database.json
  settings_adrss="vu_speech/vu_speech/database.json"
  db_user=$(jq -r ".default.USER" $settings_adrss)
  db_pass=$(jq -r ".default.PASSWORD" $settings_adrss)
  db_name=$(jq -r ".default.NAME" $settings_adrss)
  db_adrss=$(jq -r ".default.HOST" $settings_adrss)

  # fetch mysql root user credential
  db_root_file=db_root_credential.json
  mysql_root_user=$(jq -r ".default.USER" $db_root_file)
  mysql_root_pass=$(jq -r ".default.PASSWORD" $db_root_file)
}

