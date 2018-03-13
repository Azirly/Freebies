#Current Date Edit on top
#6/11/2016

#Issues with using Facebook API


import facebook
import time
import sys
import pprint

import json
import pyrebase
#Structs Here
Tokens =  {
    "Access": 'EAAFcoWXhczcBAGxlcesL6y02tbPYslQ9ROJWb45nThaJZBv6r4ew3IEwGH75v9SIMElUZBeclEbHZB7YHRnQpnt88KnbZBmQVGUdmgVzaDQ5Bb0wbESGbCyo86xOmKkPZARdVLDIuxMgX8CjE5VmebVfbatNEL2dOQcIP10iDJgZDZD'
}

config = {
  "apiKey": "AIzaSyD5n4RIY10VbNre3yBRQirban_iKdn17U4",
  "authDomain": "free-bees-b7d1a.firebaseapp.com",
  "databaseURL": "https://free-bees-b7d1a.firebaseio.com",
  "storageBucket": "free-bees-b7d1a.appspot.com",
  "serviceAccount": "../CS 125/free-bees-b7d1a-firebase-adminsdk-bhgrr-b8cc0f8df8.json"
}

firebase = pyrebase.initialize_app(config)


attachment = {
    'name': '',
    'link': '',
    'caption': '',
    'description': '',
    'picture': ''
    }

post ='''Who's going to be in Irvine over the summer?'''

def uprint(*objects, sep=' ', end='\n', file=sys.stdout):
    enc = file.encoding
    if enc == 'UTF-8':
        print(*objects, sep=sep, end=end, file=file)
    else:
        f = lambda obj: str(obj).encode(enc, errors='backslashreplace').decode(enc)
        print(*map(f, objects), sep=sep, end=end, file=file)


def main():
    #Creates the graph from the Graph API from Facebook
    # print(Tokens) 
    graph = facebook.GraphAPI(access_token=Tokens['Access'], version="2.2")
    # events = graph.search(type='event')
    # events = graph.get_object(id='158348781378124')
    # events = graph.request('/search?q=Free&q=Food&type=event&limit=1') #finding an event just through search
    events = graph.request('search', {'center': '33.640495,-117.844296',
                                      'q': 'free irvine', #space values out and they will be searched through facebook's api
                                      'fields': 'name,description,place,hours',
                                      'type': 'event', 
                                      'limit': '3',
                                      'center': '33.640495,-117.844296',
                                      'distance': '100'}) #finding an event just through search

    ###Now figure out how to store it
    ###Then figure out how to store them into firebase 
    json_events = json.dumps(events['data'])
    loaded_json_events = json.loads(json_events)
    uprint(events['data'])
    
    '''
    For each element in the array, add it into firebase
    '''
    db = firebase.database()
    db.child("users").push(events['data'])
    
    
    ###Write to a json file
    # file = open("testfile.json","w") 
    # file.write(json_events)
    # file.close()

    '''
    Then figure out a search system
    '''
'''
Micro report
Continuous stream of data

Save data in a txt file, return data
https://stackoverflow.com/questions/11181480/how-can-i-query-public-facebook-events-by-location-city

'''

def post_user_message(da_graph, fb_page_id):
    successful_post = da_graph.put_wall_post(post,attachment, profile_id = fb_page_id)
    return successful_post

def post_group_message(graph,fb_page_id):

##    groups = [ 'groupid1', 'groupid2', 'groupid3']
##    for group_id in groups:
##	print "Posting to " + 'http://www.facebook.com/groups/' + str(group_id)
##	graph.post(path =str(group_id) + '/feed', message=message)
##	time.sleep(10)
##  
    successful_post = graph.put_wall_post(post,attachment,profile_id = fb_page_id)
    return successful_post


if __name__ == "__main__":
    main()

