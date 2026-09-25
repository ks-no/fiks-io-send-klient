# Fiks IO send klient
[![MIT Licens](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/ks-no/fiks-io-send-klient/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/no.ks.fiks/fiks-io-send-klient.svg)](https://search.maven.org/search?q=g:no.ks.fiks%20a:fiks-io-send-klient)
![GitHub last commit](https://img.shields.io/github/last-commit/ks-no/fiks-io-send-klient.svg)
![GitHub Release Date](https://img.shields.io/github/release-date/ks-no/fiks-io-send-klient.svg)

Enkel send-klient for Fiks IO i JVM miljø. Denne klienten brukes for å sende inn ASiC-E signerte meldinger til Fiks IO over HTTPS.

Fiks IO er en kanal for sikker maskin-til-maskin integrasjon på tvers av organisasjoner og systemer. [Om Fiks IO](https://ks-no.github.io/fiks-plattform/tjenester/fiksprotokoll/fiksio/)

## Forenkler sending av meldinger
Denne send-klienten forenkler sending av meldinger til Fiks IO ved å håndtere signeringen (ASiC-E), krypteringen og de påkrevde [headerene](https://ks-no.github.io/fiks-plattform/tjenester/fiksprotokoll/fiksio/#headere) automatisk.

> **Merk:** Man må også sette opp en klient som abonnerer på meldinger til kontoen, bl.a. for å motta kvitteringer og feilmeldinger. Se:
> - Java: [fiks-io-klient-java](https://github.com/ks-no/fiks-io-klient-java)
> - .NET: [fiks-io-client-dotnet](https://github.com/ks-no/fiks-io-client-dotnet)

> **Merk:** Man må også sette opp en klient som abonnerer på meldinger til kontoen, bl.a. for å motta kvitteringer og feilmeldinger. Se:
> - Java: [fiks-io-klient-java](https://github.com/ks-no/fiks-io-klient-java)
> - .NET: [fiks-io-client-dotnet](https://github.com/ks-no/fiks-io-client-dotnet)

## Versjoner
Versjon 3.x krever Java 17 eller høyere. Har du ikke mulighet for å bruke det må du holde deg på v. 2.x

| Versjon | Java baseline | Spring Boot versjon | Status      |
|---------|---------------|---------------------|-------------|
| 3.x     | Java 17       | 3.X                 | Aktiv       |
| 2.X     | Java 11       | 2.X                 | Vedlikehold |

## Ta i bruk

### Maven
Legg til følgende i POM-filen din:

```xml
<dependency>
    <groupId>no.ks.fiks</groupId>
    <artifactId>fiks-io-send-klient</artifactId>
    <version>x.x.x</version>
</dependency>
```

## Bruk

### Oppsett

```java
final FiksIOUtsendingKlient klient = FiksIOUtsendingKlientBuilder.builder()
    .kontoId(new KontoId("din-konto-id"))
    .integrasjonId("din-integrasjons-id")
    .integrasjonPassord("ditt-integrasjons-passord")
    .privatNokkel(privatKeyString)
    .mottakerKontoId(new KontoId("mottaker-konto-id"))
    .mottakerPublicCertifikat(publicCertificate)
    .build();

// Sende melding
final SendtMelding sendtMelding = klient.send(payload, meldingType);
```


## Dokumentasjon

* [Fiks IO](https://ks-no.github.io/fiks-plattform/tjenester/fiksio/)
