Firebase!

(create a project in the firebase console, download the google-services.json file and add the proper dependencies into the project level and app level gradle file).
The NearIt SDK already has the dependency related to the FCM messaging service and has registered the proper service and receivers for handling our internal push notification resolution in its manifest. 
The data will be fetched directly from the config file (google-services.json).