# ClientModul Project

## Overview

The `ClientModul` project is designed to filter out unnecessary RSA certificates from a list of certificates. It ensures that if an ECC certificate from the same SMC-B is present, the corresponding RSA certificate is removed.

## Features

- Filters RSA certificates if an ECC certificate with the same ICCSN is present.
- Handles certificates based on their `validFrom` date to ensure they are within a specified time window.
- Provides utility methods to handle certificate comparisons and filtering.

## Prerequisites

- Java
- Gradle
- IntelliJ IDEA (recommended)

## Getting Started

1. **Clone the repository:**

    ```sh
    git clone <repository-url>
    cd <repository-directory>
    ```

2. **Open the project in IntelliJ IDEA:**

    - Open IntelliJ IDEA.
    - Select `File > Open` and choose the cloned repository directory.

3. **Build the project:**

    ```sh
    ./gradlew build
    ```

4. **Run the tests:**

    ```sh
    ./gradlew test
    ```

## Usage

To use the `ClientModul` class in the desired variant, instantiate it and call the `weedOutUnnecessaryRsaCerts` method with a list of certificates:

```groovy
def clientModul = new ClientModul_<VariantNumber>()
def filteredCerts = clientModul.weedOutUnnecessaryRsaCerts(certs)
```

## License

Copyright 2023 gematik GmbH
 
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 
See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License.
 
Unless required by applicable law the software is provided "as is" without warranty of any kind, either express or implied, including, but not limited to, the warranties of fitness for a particular purpose, merchantability, and/or non-infringement. The authors or copyright holders shall not be liable in any manner whatsoever for any damages or other claims arising from, out of or in connection with the software or the use or other dealings with the software, whether in an action of contract, tort, or otherwise.
 
The software is the result of research and development activities, therefore not necessarily quality assured and without the character of a liable product. For this reason, gematik does not provide any support or other user assistance (unless otherwise stated in individual cases and without justification of a legal obligation). Furthermore, there is no claim to further development and adaptation of the results to a more current state of the art.
 
Gematik may remove published results temporarily or permanently from the place of publication at any time without prior notice or justification.