ps -ef | grep "vu_speech/manage.py runserver" | while read -r line ; do
  server_pid=$(echo $line | awk '{print $2}')
  kill -9 $server_pid
done
