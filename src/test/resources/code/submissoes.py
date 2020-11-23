import requests
import time 

file = open('submissoes.in', 'r')

chars_to_remove = ['|', '|', ' ', '\n']

for line in file:	
    url = 'http://localhost:8080/api/submissions/%s/reevaluate' % line.translate(None, ''.join(chars_to_remove))
    r = requests.post(url, headers = {"Authorization": "Basic YWRtaW46aHV4bGV5MTIzISE="}, data={})
    print(r.status_code, r.reason)    
    #break
    #time.sleep(1)