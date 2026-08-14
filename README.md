# Collectors Yays and Nays

A small Spring Boot application for processing Magic: The Gathering card scans exported from ManaBox and generating Moxfield-ready import lists.

The project was created to make processing large batches of scanned cards easier. It keeps track of cards that have already been encountered, checks new cards against Scryfall, and separates them into cards worth adding to a collection and low-value cards that may need manual review.

## How it works

The application accepts a ManaBox scan exported as a CSV file.

For every scanned card it:

1. Reads the card's Scryfall ID from the ManaBox export.
2. Fetches the card's Oracle ID and EUR price from Scryfall.
3. Checks whether the same card has appeared in a previous scan.
4. Classifies previously unseen cards based on price.
5. Generates Moxfield-compatible text files.
6. Updates a persistent history of previously scanned cards.

Cards are considered duplicates based on their **Scryfall Oracle ID**.

This means different printings, sets and foil/non-foil versions of the same underlying card are treated as the same card for duplicate detection.

## Classification

Cards are divided into three categories:

| Category | Rule |
| --- | --- |
| Collection worthy | New card with a price of at least €0.10 |
| Maybe | New card below €0.10 or without available EUR price data |
| Previously seen | The card's Oracle ID already exists in scan history |

The price threshold is currently fixed at **€0.10**.

## Output

After processing a scan, the application generates:

```text
data/
├── collection-worthy.txt
├── maybe.txt
└── seen-cards.csv
```

### `collection-worthy.txt`

Contains cards from the current scan which pass the price threshold.

Example:

```text
1 Mana Tithe (STA) 8
1 Sol Ring (CMM) 396
1 Iron-Shield Elf (ECL) 404 *F*
```

The output is formatted for importing into Moxfield.

### `maybe.txt`

Contains low-value cards and cards for which a EUR price could not be determined.

Both `collection-worthy.txt` and `maybe.txt` are recreated for every scan and only contain results from the most recent scan.

### `seen-cards.csv`

Stores the Oracle IDs of cards encountered across previous scans.

Unlike the two output files, this file is persistent and is used for duplicate detection on future scans.

## Scryfall requests

Card information and EUR pricing are retrieved using the Scryfall API.

Requests are rate limited to a maximum of approximately **5 requests per second** in order to avoid excessive API traffic.

## Requirements

- Java 21
- Internet connection for Scryfall lookups

The repository contains the Maven Wrapper, so a separate Maven installation is not required.

## Running the application

On Windows:

```bash
mvnw.cmd spring-boot:run
```

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

The application runs on port `8080` by default.

## Processing a scan

Send a `POST` request to:

```text
http://localhost:8080/classify
```

using `multipart/form-data`.

The uploaded file must use the form field:

```text
file
```

For example with curl:

```bash
curl -X POST -F "file=@manabox-scan.csv" http://localhost:8080/classify
```

The endpoint also returns the classification result as JSON.

## Tech stack

- Java 21
- Spring Boot
- Spring Web MVC
- Apache Commons CSV
- Lombok
- Scryfall API

## Current limitations

- Designed specifically around ManaBox scan CSV exports.
- The €0.10 threshold is currently hardcoded.
- Duplicate detection deliberately ignores different printings of the same card.
- Scryfall EUR prices are used as the pricing source.
- The application currently exposes a REST API and does not have a graphical interface.

## Possible future improvements

- Configurable price threshold
- Simple browser interface for uploading scans
- Scryfall response caching
- Better handling of failed API requests
- Additional automated tests
- Configurable duplicate detection rules

## Disclaimer

This is a personal project created for personal use and it is not affiliated with ManaBox, Moxfield, Scryfall, Wizards of the Coast, or Hasbro.
