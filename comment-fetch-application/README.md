# comment-youtube-ranker-backend

This java program uses the **[YouTube Data V3 API](https://developers.google.com/youtube/v3)** to fetch comments from the days top charts, then stores them in a PostGreSQL database.

Environment Variables:

> POSTGRES

* DATABASE_PORT
* DATABASE_NAME
* DATABASE_USER
* DATABASE_PASSWORD

> YOUTUBE API:

* [YOUTUBEDATAV3APIKEY](https://console.cloud.google.com/apis/credentials)
* QUOTA_REMAINING

> PGADMIN (Optional):

* PGADMIN_DEFAULT_EMAIL
* PGADMIN_DEFAULT_PASSWORD
