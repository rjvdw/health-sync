import yaml
import garth

username = None
password = None

with open('.garth/auth.yaml') as stream:
    data = yaml.safe_load(stream)
    username = data['auth']['username']
    password = data['auth']['password']

garth.login(username, password)
garth.save('.garth/session')
