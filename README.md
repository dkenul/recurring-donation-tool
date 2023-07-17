# Recurring Donation Tool

A recurring donation command-line tool implemented in Scala.

*Note: This implementation is created as an exercise and not intended for real-world use.*

## Basic Usage

Given some text file `input.txt` containing newline-separated commands:

```
cat input.txt | ./gfm-recurring
./gfm-recurring input.txt
```

The tool will parse supported commands line by line and print a summary of the processed data with the following format:

```
Donors:
[donor name]: Total: $[sum of donations] Average: $[average of donations]
...[all donors in alphabetic order]

Campaigns:
[campaign name]: Total: $[sum of donations]
...[all campaigns in alphabetic order]

```

For example, given the following input:
```
Add Donor Greg $1000
Add Donor Janine $100
Add Campaign SaveTheDogs
Add Campaign HelpTheKids
Donate Greg SaveTheDogs $100
Donate Greg HelpTheKids $200
Donate Janine SaveTheDogs $50
```

We would expect the following output:
```
Donors:
Greg: Total: $300 Average: $150
Janine: Total: $50 Average: $50

Campaigns:
HelpTheKids: Total: $200
SaveTheDogs: Total: $150
```


## Supported Commands

#### Add Donor
```
Add Donor [name] $[monthly limit]
```

#### Add Campaign
```
Add Campaign [name]
```

#### Donate
```
Donate [donor name] [campaign name] $[amount]
```


## Behavior

#### Naming

Pertains to: `Add Donor [name]` and `Add Campaign [name]`
* Names serve as unique identifiers
* Names do not contain spaces
* In the case of a naming collision within the same entity, the command will be skipped

#### Currency

Pertains to: `Add Donor ... [monthly limit]` and `Donate ... [amount]`
* Currency must have a prepended symbol to be considered valid
    * Only `$` symbol will be supported for the sake of this exercise
* Decimals will be accepted but must be in valid currency format
    * `$100.01` will be accepted
    * `$100.011` will not be accepted

#### Processing

* Lines are processed sequentially
* Invalid inputs will be skipped
* Donations over the defined monthly limit will be skipped
* Leading and trailing newlines will be allowed

#### Data Persistence

* Tool will not persist data among separate executions

#### Output

* Output will be printed to the command line only (not saved to an output file)
* Output will contain all added donors and campaigns, regardless of donations (zero-donation entities)
* Donors and Campaigns will be printed alphabetically
* Empty input will still result in the provided output format
	* See corresponding [Integration Test](integration-tests/09-empty-input/output.txt) for an exact output
* Currency will be formatted such that decimals are removed for zero-cent values
    * `$100.50` -> `$100.50`
    * `$100.00` -> `$100`

## Setup

I've attempted to provide multiple setup options since Scala applications can be cumbersome to set up.

#### Minimal

**Linux**: The default `gfm-recurring` script provided in this repo can be executed as a native binary in Linux systems.

As such, to run the script you should only need to download the repository and run:
```
./gfm-recurring input.txt
cat input.txt | ./gfm-recurring
```

**MacOS**: Run the native binary found in in `./bin/osx/gfm-recurring`
```
./bin/osx/gfm-recurring input.txt
cat input.txt | ./bin/osx/gfm-recurring
```


#### Docker

A Dockerfile is included which can be used to set up a fully containerized environment.

*Note: The container is configured to run infinitely for development purposes. Make sure to stop the process after you are finished*

1. Download Docker [Here](https://www.docker.com/)
2. Make sure the Docker daemon is running on your system
3. Navigate to the downloaded git repository
3. Build the image
	```
	docker build -t gfm-recurring-image .
	```
4. Start the container
	```
    docker run gfm-recurring-image
    ```
5. In a new terminal window, Find the container ID
	```
    docker ps
    ```
6. Open a shell inside the container:
	```
    docker exec -it [container id] bash
    ```
7. Your working directory should be `/app` which is set up for development.
8. Start the sbt console: 
   ```
   sbt
   ```

From the sbt console you can run the program manually:

```
run input.txt
```

Or run the unit and integration tests
```
test
```

#### Manual

1. Install your desired JDK 20 flavor - I used [Temurin](https://adoptium.net/temurin/releases/?version=20)
2. Download [Coursier](https://get-coursier.io/docs/cli-installation)
2. Follow setup instructions for your desired system
3. You should now have sbt installed and can open the sbt console
4. `sbt`
5. `run input.txt` or `test`


## Discussion

This is an interesting exercise because there are multiple implementation details and edge-cases to consider. The core concept of my solution is to provide a structure that is extensible, resillient, testable, and maintainable.

Design decisions that contribute to this include:
* Using a type-safe language which promotes resillience.
* Favoring immutablility to ensure that each application component is testable and maintainable.
* Isolating state management to `helpers/DataManager`. In the future, these methods can be updated to query a real database rather than using in-memory data structures.
* Isolating command logic to `models/Command`. This file provided an abstract `Command` trait. Each supported command extends this trait which promotes extensibility and maintainability. The steps required to author new commands would be:
	1. Implement desired parsing and validation logic in `Command` companion object's `apply` method. 
	2. Implement a new class for the command extending the `Command` trait with a corresponding `execute` method.

#### Limitations

In retrospect, I would have liked to create a more clear and isolated validation flow. Currently, there is a limited amount of validation which can be found in `models/Command`. However, validation functions should be extracted into their own helper and supplied with Unit tests.

I also opted for an eager processing approach - aka - validation and execution errors are cataloged and ignored. This may not be desirable depending on application needs. For example, we may want to log warnings for invalid commands or execution erros. We may want to ensure that all commands are valid before exeuting any. We may also want to execute valid commands until we encounter the first error.



## Testing

I've included both Unit and Integration tests for this problem (found in `src/test/scala/`), but given time contraints, I did not prioritize Unit test coverage.

Files consumed by the integration tests can be found in `integration-tests/[test folder]` where each test folder contains a given `input.txt` and an expected `output.txt`. While I am currently running these using ScalaTest for convenience, they could easily be moved to a CI tool like GitHub Actions. 
