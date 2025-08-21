#!/bin/bash

invalidVariables=0

if [[ -z "${DATABASE_PORT}" ]]; then
	echo "Missing environment variable: DATABASE_PORT"
	invalidVariables=1
fi

if [[ -z "${DATABASE_NAME}" ]]; then
	echo "Missing environment variable: DATABASE_NAME"
	invalidVariables=1
fi

if [[ -z "${DATABASE_USER}" ]]; then
	echo "Missing environment variable: DATABASE_USER"
	invalidVariables=1
fi

if [[ -z "${DATABASE_PASSWORD}" ]]; then
	echo "Missing environment variable: DATABASE_PASSWORD"
	invalidVariables=1
fi

if [[ -z "${QUOTA_REMAINING}" ]]; then
	echo "Missing environment variable: QUOTA_REMAINING"
	invalidVariables=1
fi

if [[ -z "${YOUTUBEDATAV3APIKEY}" ]]; then
	echo "Missing environment variable: YOUTUBEDATAV3APIKEY"
	invalidVariables=1
fi

if [ $invalidVariables -eq 1 ]; then
	echo "Exiting"
	exit 1
fi

remainingQuota=${QUOTA_REMAINING}
totalCommentsFetched=0

TMPFILE=$(mktemp)

echo "Temporary file:"

source $TMPFILE

until [ $remainingQuota -lt 1 ]; do
  echo "Remaining quota is $remainingQuota"
  java -cp ./target/YoutubeCommentFetchApplication-1.0-SNAPSHOT-jar-with-dependencies.jar org.myapps.youtube.commentranker.YoutubeCommentRankerApp > $TMPFILE
  source $TMPFILE
  totalCommentsFetched=$(( $totalCommentsFetched + ${commentsFetched} ))
  ((remainingQuota--))
done

echo "Quota expended, total comments fetched $totalCommentsFetched"
