# Digiroad2 TN-ITS #

[![Circle CI](https://circleci.com/gh/finnishtransportagency/tn-its.svg?style=svg)](https://circleci.com/gh/finnishtransportagency/tn-its)

This repository contains the [Finnish Transport Agency](http://www.liikennevirasto.fi/web/en) [TN-ITS](http://tn-its.eu) API and conversion implementations.

The application is live at http://tn-its.herokuapp.com/rosattedownload/download/queryDataSets.

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

### TN-ITS API

```sh
make run
```

Open [http://localhost:8090/RosatteDownload/download/querydatasets](http://localhost:8090/RosatteDownload/download/querydatasets).

### Conversion

```sh
make convert
```

## Deployment

This repository is deployed automatically to Heroku on each push to the master branch.

