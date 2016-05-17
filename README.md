# Digiroad2 TN-ITS

[![Circle CI](https://circleci.com/gh/finnishtransportagency/tn-its.svg?style=svg)](https://circleci.com/gh/finnishtransportagency/tn-its)

This repository contains the [Finnish Transport Agency](http://www.liikennevirasto.fi/web/en) [TN-ITS](http://tn-its.eu) API and conversion implementations.

The application is live at http://tn-its.herokuapp.com/rosattedownload/download/queryDataSets.

## Configuration

| Environment variable     | Description                                             | Required   | Default   |
| ------------------------ | -------------                                           | ---------- | --------- |
| AINEISTOT_USERNAME       | Dataset FTP username                                    | Yes        |           |
| AINEISTOT_PASSWORD       |                                                         | Yes        |           |
| AINEISTOT_DIRECTORY      |                                                         | Yes        |           |
| CHANGE_API_USERNAME      | OTH API username                                        | Yes        |           |
| CHANGE_API_PASSWORD      | OTH API password                                        | Yes        |           |
| CHANGE_API_URL           | OTH hostname and path to change API                     | Yes        |           |
| CONVERTER_API_USERNAME   | Converter endpoint (/conversion) username               | Yes        |           |
| CONVERTER_API_PASSWORD   | Converter password                                      | Yes        |           |
| QUOTAGUARDSTATIC_URL     | When set, provides static IP for requests to change API | No         |           |
| PORT                     | Listen on this port                                     | No         | 8090      |

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

## Code

The [fi.liikennevirasto.digiroad2.tnits.runners](src/main/scala/fi/liikennevirasto/digiroad2/tnits/runners) package
contains the entry points to this project.
There are two runnable programs:
[Converter](src/main/scala/fi/liikennevirasto/digiroad2/tnits/runners/Converter.scala)
and
[Api](src/main/scala/fi/liikennevirasto/digiroad2/tnits/runners/Api.scala).
The latter is run in production.
The former is for development and not required.
