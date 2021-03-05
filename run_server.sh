source env/bin/activate
script_file=vu_speech/manage.py
python3 $script_file migrate
python3 $script_file runserver
