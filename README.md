# Prepaid balance

This app keeps track of your prepaid balance. It can automatically record it and post a notification if your balance drops under a configurable threshold.
It does so by parsing the response of a USSD code, that can be set in the settings.

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/de/packages/com.github.muellerma.prepaidbalance/)

## Translations

You can help translating this app by coping the file `app/src/main/res/values/strings.xml` to `app/src/main/res/values-xx/strings.xml` (`xx` stands for your language code), removing all entries marked with `translatable="false"` and translating the remaining entries. This file contains all strings used in the app.

The app store description can also be translated: Copy `fastlane/metadata/android/en-US/full_description.txt` and `fastlane/metadata/android/en-US/short_description.txt` to `fastlane/metadata/android/xx-YY/full_description.txt` (`xx` is the language code, `YY` is the country code) and translate the file.

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
