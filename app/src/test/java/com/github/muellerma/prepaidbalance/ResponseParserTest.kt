package com.github.muellerma.prepaidbalance

import com.github.muellerma.prepaidbalance.utils.ResponseParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResponseParserTest {
    @Test
    fun testInvalidMessages() {
        listOf(
            null,
            "",
            "foobar",
            "Tu solicitud no puede ser tramitada en este momento. Por favor vuelve a ingresar llamando al *888#",
            "Dieser Service steht auf Grund von Wartungsarbeiten leider erst ab 9 Uhr wieder zur Verfügung.",
            "Wir können Deine Anfrage derzeit leider nicht bearbeiten. Bitte versuche es später erneut oder unter 7. (8)",
        ).forEach { message ->
            assertNull(ResponseParser.getBalance(message))
        }
    }

    @Test
    fun testValidMessages() {
        mapOf(
            "2.42" to 2.42,
            "02.42" to 2.42,
            "2,42" to 2.42,
            "02,42" to 2.42,
            "1202,42" to 1202.42,
            "1202" to 1202.0 ,
            "1202.0" to 1202.0 ,
            "1202.00" to 1202.0 ,
            "5.12 EUR" to 5.12,
            "Current balance is 5.12 EUR" to 5.12,
            "Current balance: 5.12 EUR." to 5.12,
            "Current balance: 5.12 EUR. foobar" to 5.12,
            "5.12 EUR is the current balance" to 5.12,
            "Some ads; 5.12 EUR is the current balance" to 5.12,
            "Some ads. 5.12 EUR is the current balance" to 5.12,
            "Current balance is 5,12 EUR" to 5.12,
            "Current balance is 5.12 EURO" to 5.12,
            "Current balance is 5.12 €" to 5.12,
            "Current balance is 5,12 EUR." to 5.12,
            "Current balance is 5.12 EURO." to 5.12,
            "Current balance is 5.12 €." to 5.12,
            "Current balance is € 5.12" to 5.12,
            "Current balance is 5.12 USD" to 5.12,
            "Current balance is \$ 5.12" to 5.12,
            "Current balance is 5.12" to 5.12,
            // Vodafone Germany
            "Aktuelles Guthaben: 5,12 EUR\nWähl bitte aus:\n1 Aufladen\n2 Guthaben & Verbrauch\n3  Tarife & Optionen\n4 Spracheinstellungen\n5 Vorteilsangebot" to 5.12,
            // https://github.com/mueller-ma/PrepaidBalance/issues/11#issuecomment-977514814
            "Pulsa Rp 1234 s.d. 01-01-2022 \n1 8GB,Rp30rb/30hr\n2 25GB,Rp50rb/30hr\n22 Info" to 1234.0,
            // Polish Plus
            "Aktualny stan konta dla numeru 48123456789: 1,23 PLN. Konto wazne do dnia 01-01-2022 01:02:03.Doladuj PLUSem!" to 1.23,
            // Kaufland mobil Germany
            "Dein Guthaben betraegt: 10,00 EUR Startguthaben, 20,00 EUR Kaufguthaben und 40,00 EUR Geschenkguthaben." to 70.0,
            "Dein Guthaben betraegt: 0,00 EUR Startguthaben, 0,00 EUR Kaufguthaben und 0,00 EUR Geschenkguthaben." to 0.0 ,
            "Dein Guthaben betraegt: 10,00 EUR Startguthaben, 0,00 EUR Kaufguthaben und 0,00 EUR Geschenkguthaben." to 10.0,
            "Dein Guthaben betraegt: 10,00 EUR Startguthaben und 20,00 EUR Kaufguthaben." to 30.0,
            "Dein Guthaben betraegt: 20,00 EUR Kaufguthaben." to 20.0,
            // T-Mobile US
            "Current plan active until 08/10/2022 and will renew for \$12.34. Account Balance \$56.78. Add money by dialing *233 or redeem a refill card." to 56.78,
            // Sunrise Switzerland
            "Ihr Guthaben beträgt CHF 12.34" to 12.34,
            "Ihr Guthaben beträgt CHF 12.34." to 12.34,
            // O2 UK
            "O2: Your balance is £6.45. Text BALANCE free to 20202 to check your remaining tariff and Bolt On allowances." to 6.45,
            "O2: Your balance is £12.34. Text BALANCE free to 20202 to check your remaining tariff and Bolt On allowances." to 12.34,
            // https://github.com/mueller-ma/PrepaidBalance/issues/160
            "Twoja oferta to nju na karte.\n1.Stan konta glownego: 11.00 zl. Srodki wazne bezterminowo." to 11.0,
            // https://github.com/mueller-ma/PrepaidBalance/issues/188
            "Dobry den, aktualni vyse Vaseho kreditu na cisle 12345678 je 300 Kc, z toho bonusovy kredit je 0 Kc. Platnost bezneho kreditu: 30. 1. 2024 18:18. Vase O2" to 300.0,
            // https://silent.link
            "IMSI:123456789012345\n USD:5.87\n Num#:123456789012" to 5.87
        ).forEach { (message, balance) ->
            assertEquals(message, balance, ResponseParser.getBalance(message))
        }
    }
}
