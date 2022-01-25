# Prepaid balance

[![Build App](https://github.com/mueller-ma/PrepaidBalance/actions/workflows/build.yml/badge.svg)](https://github.com/mueller-ma/PrepaidBalance/actions/workflows/build.yml)
[![CodeFactor](https://www.codefactor.io/repository/github/mueller-ma/prepaidbalance/badge)](https://www.codefactor.io/repository/github/mueller-ma/prepaidbalance)
[![Crowdin](https://badges.crowdin.net/prepaidbalance/localized.svg)](https://crowdin.com/project/prepaidbalance)

This app keeps track of your prepaid balance. It can automatically record it and post a notification if your balance drops under a configurable threshold.
It does so by parsing the response of a USSD code, that can be set in the settings.

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/de/packages/com.github.muellerma.prepaidbalance/)

## Translations

App strings and the app store description can be translated via Crowdin: https://crowdin.com/project/prepaidbalance

Translations have to be approved before being merged into the app. To become a translator with approval rights or to request a new language, please [poke me on Crowdin](https://crowdin.com/profile/mueller-ma) or open an issue here on GitHub. 

## Troubleshooting

You might run into on of these issues with the app:
* The USSD code is different than the default (`*100#`). In this case please configure the correct one and open an issue, so I can change the default code for your provider. 
* Your provider doesn't provide a USSD code to query your prepaid balance. In this case you cannot use this app.
* Depending on the response pattern the parsing might fail. In this case please open an issue.

## Android permissions

* `CALL_PHONE`: Used to query USSD codes.
* `ACCESS_NETWORK_STATE`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`, `WAKE_LOCK`: Used by [WorkManager](https://developer.android.com/reference/androidx/work/WorkManager) to ensure work constraints are met and running workers aren't killed.

## Credits

* Feature graphic by https://unsplash.com/@joshappel
