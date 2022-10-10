package com.github.muellerma.prepaidbalance

import com.github.muellerma.prepaidbalance.utils.ResponseParser
import org.junit.Assert.assertEquals
import org.junit.Test

class ResponseParserTest {
    @Test
    fun testGetBalance() {
        assertEquals(null, ResponseParser.getBalance(null))
        assertEquals(null, ResponseParser.getBalance(""))
        assertEquals(null, ResponseParser.getBalance("foobar"))
        assertEquals(2.42, ResponseParser.getBalance("2.42"))
        assertEquals(2.42, ResponseParser.getBalance("2,42"))
        assertEquals(2.42, ResponseParser.getBalance("02,42"))
        assertEquals(1202.42, ResponseParser.getBalance("1202,42"))
        assertEquals(1202.0, ResponseParser.getBalance("1202"))
        assertEquals(1202.0, ResponseParser.getBalance("1202.0"))
        assertEquals(1202.0, ResponseParser.getBalance("1202.00"))

        assertEquals(5.12, ResponseParser.getBalance("5.12 EUR"))
        assertEquals(5.12, ResponseParser.getBalance("Current balance is 5.12 EUR"))
        assertEquals(5.12, ResponseParser.getBalance("Current balance: 5.12 EUR."))
        assertEquals(5.12, ResponseParser.getBalance("Current balance: 5.12 EUR. foobar"))
        assertEquals(5.12, ResponseParser.getBalance("5.12 EUR is the current balance"))
        assertEquals(5.12, ResponseParser.getBalance("Some ads; 5.12 EUR is the current balance"))
        assertEquals(5.12, ResponseParser.getBalance("Some ads. 5.12 EUR is the current balance"))
        assertEquals(5.12, ResponseParser.getBalance("Current balance is 5,12 EUR"))
        assertEquals(5.12, ResponseParser.getBalance("Current balance is 5.12 EURO"))
        assertEquals(5.12, ResponseParser.getBalance("Current balance is 5.12 €"))
        assertEquals(5.12, ResponseParser.getBalance("Current balance is € 5.12"))
        assertEquals(5.12, ResponseParser.getBalance("Current balance is 5.12 USD"))
        assertEquals(5.12, ResponseParser.getBalance("Current balance is $ 5.12"))
        assertEquals(5.12, ResponseParser.getBalance("Current balance is 5.12"))

        // Vodafone Germany
        assertEquals(5.12, ResponseParser.getBalance("Aktuelles Guthaben: 5,12 EUR\n" +
                "Wähl bitte aus:\n" +
                "1 Aufladen\n" +
                "2 Guthaben & Verbrauch\n" +
                "3  Tarife & Optionen\n" +
                "4 Spracheinstellungen\n" +
                "5 Vorteilsangebot")
        )

        // https://github.com/mueller-ma/PrepaidBalance/issues/11#issuecomment-977514814
        assertEquals(1234.0, ResponseParser.getBalance("Pulsa Rp 1234 s.d. 01-01-2022 \n" +
                "1 8GB,Rp30rb/30hr\n" +
                "2 25GB,Rp50rb/30hr\n" +
                "22 Info")
        )

        // Polish Plus
        assertEquals(
            1.23,
            ResponseParser.getBalance(
                "Aktualny stan konta dla numeru 48123456789: 1,23 PLN. Konto wazne do dnia 01-01-2022 01:02:03.Doladuj PLUSem!"
            )
        )

        // Kaufland mobil Germany
        assertEquals(
            70.0,
            ResponseParser.getBalance(
                "Dein Guthaben betraegt: 10,00 EUR Startguthaben, 20,00 EUR Kaufguthaben und 40,00 EUR Geschenkguthaben."
            )
        )
        assertEquals(
            0.0,
            ResponseParser.getBalance(
                "Dein Guthaben betraegt: 0,00 EUR Startguthaben, 0,00 EUR Kaufguthaben und 0,00 EUR Geschenkguthaben."
            )
        )
        assertEquals(
            10.0,
            ResponseParser.getBalance(
                "Dein Guthaben betraegt: 10,00 EUR Startguthaben, 0,00 EUR Kaufguthaben und 0,00 EUR Geschenkguthaben."
            )
        )
        assertEquals(
            30.0,
            ResponseParser.getBalance(
                "Dein Guthaben betraegt: 10,00 EUR Startguthaben und 20,00 EUR Kaufguthaben."
            )
        )
        assertEquals(
            20.0,
            ResponseParser.getBalance(
                "Dein Guthaben betraegt: 20,00 EUR Kaufguthaben."
            )
        )

        // T-Mobile US
        assertEquals(
            56.78,
            ResponseParser.getBalance("Current plan active until 08/10/2022 and will renew for \$12.34. Account Balance \$56.78. Add money by dialing *233 or redeem a refill card.")
        )

        // Sunrise Switzerland
        assertEquals(
            12.34,
            ResponseParser.getBalance("Ihr Guthaben beträgt CHF 12.34")
        )

        // O2 UK
        assertEquals(
            6.45,
            ResponseParser.getBalance("O2: Your balance is £6.45. Text BALANCE free to 20202 to check your remaining tariff and Bolt On allowances.")
        )
    }
}