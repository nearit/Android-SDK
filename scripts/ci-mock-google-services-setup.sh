#!/usr/bin/env bash

## Never run this on your dev machine or it will override your valid google-services.json file!

echo '{
  "project_info": {
    "project_number": "1234567894652",
    "firebase_url": "https://near-mock.firebaseio.com",
    "project_id": "near-mock",
    "storage_bucket": "near-mock.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:123456789000:android:f1bf012572b04063",
        "android_client_info": {
          "package_name": "com.nearit.sample"
        }
      },
      "oauth_client": [
        {
          "client_id": "XXXXXXXXXXXX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.apps.googleusercontent.com",
          "client_type": 1,
          "android_info": {
            "package_name": "com.nearit.sample",
            "certificate_hash": "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
          }
        },
        {
          "client_id": "XXXXXXXXXXXX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.apps.googleusercontent.com",
          "client_type": 1,
          "android_info": {
            "package_name": "com.nearit.sample",
            "certificate_hash": "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
          }
        },
        {
          "client_id": "XXXXXXXXXXXX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.apps.googleusercontent.com",
          "client_type": 3
        },
        {
          "client_id": "XXXXXXXXXXXX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.apps.googleusercontent.com",
          "client_type": 3
        }
      ],
      "api_key": [
        {
          "current_key": "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
        }
      ],
      "services": {
        "analytics_service": {
          "status": 2,
          "analytics_property": {
            "tracking_id": "UA-123456-78"
          }
        },
        "appinvite_service": {
          "status": 1,
          "other_platform_oauth_client": []
        },
        "ads_service": {
          "status": 2
        }
      }
    }
  ],
  "configuration_version": "1"
}' > sample/google-services.json

echo '{
  "project_info": {
    "project_number": "1234567894652",
    "firebase_url": "https://near-mock.firebaseio.com",
    "project_id": "near-mock",
    "storage_bucket": "near-mock.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:123456789000:android:f1bf012572b04063",
        "android_client_info": {
          "package_name": "com.nearit.sample_kotlin"
        }
      },
      "oauth_client": [
        {
          "client_id": "XXXXXXXXXXXX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.apps.googleusercontent.com",
          "client_type": 1,
          "android_info": {
            "package_name": "com.nearit.sample_kotlin",
            "certificate_hash": "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
          }
        },
        {
          "client_id": "XXXXXXXXXXXX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.apps.googleusercontent.com",
          "client_type": 1,
          "android_info": {
            "package_name": "com.nearit.sample_kotlin",
            "certificate_hash": "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
          }
        },
        {
          "client_id": "XXXXXXXXXXXX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.apps.googleusercontent.com",
          "client_type": 3
        },
        {
          "client_id": "XXXXXXXXXXXX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.apps.googleusercontent.com",
          "client_type": 3
        }
      ],
      "api_key": [
        {
          "current_key": "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
        }
      ],
      "services": {
        "analytics_service": {
          "status": 2,
          "analytics_property": {
            "tracking_id": "UA-123456-78"
          }
        },
        "appinvite_service": {
          "status": 1,
          "other_platform_oauth_client": []
        },
        "ads_service": {
          "status": 2
        }
      }
    }
  ],
  "configuration_version": "1"
}' > sample-kotlin/google-services.json