# Digiroad2 TN-ITS #

[![Circle CI](https://circleci.com/gh/finnishtransportagency/tn-its.svg?style=svg)](https://circleci.com/gh/finnishtransportagency/tn-its)

## Build & Run ##

```sh
$ make run
```

Launch your browser, manually open [http://localhost:8080/RosatteDownload/download/querydatasets](http://localhost:8090/RosatteDownload/download/querydatasets) in your browser.

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


