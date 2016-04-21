# Digiroad2 TN-ITS #

[![Circle CI](https://circleci.com/gh/finnishtransportagency/tn-its.svg?style=svg)](https://circleci.com/gh/finnishtransportagency/tn-its)

## Configuration

| Environment variable   | Description | Required | Default |
|------------------------|-------------|----------|---------|
| AINEISTOT_USERNAME     |             | Yes      |         |
| AINEISTOT_PASSWORD     |             | Yes      |         |
| AINEISTOT_DIRECTORY    |             | Yes      |         |
| CHANGE_API_USERNAME    |             | Yes      |         |
| CHANGE_API_PASSWORD    |             | Yes      |         |
| CHANGE_API_URL         |             | Yes      |         |
| CONVERTER_API_USERNAME |             | Yes      |         |
| CONVERTER_API_PASSWORD |             | Yes      |         |
| PORT                   |             | No       | 8090    |

## Local development

```sh
$ make run
```

Open [http://localhost:8090/RosatteDownload/download/querydatasets](http://localhost:8090/RosatteDownload/download/querydatasets).

## Deploy

This repository is deployed automatically to Heroku on each push to the master branch.

