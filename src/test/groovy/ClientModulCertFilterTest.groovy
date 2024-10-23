import spock.lang.Specification
import spock.lang.Unroll
import spock.lang.Shared
import java.text.SimpleDateFormat

@Unroll("Class: #classUnderTest")
class ClientModulCertFilterTest extends Specification {

    @Shared
    def classesUnderTest = ["ClientModul_Variant1", "ClientModul_Variant2"]

    @Shared
    def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    def "0 - If an RSA certificate has the same ICCSN as an ECC Certificate, it shall be filtered for ICCSN."() {
        given: """
            A clientmodul with a list of 2 certificates which: 
            - hold identical ICCSNs, indicating coming from the same SMC-B \n
            - differ in their certificate type of ECC and RSA 
            - have a time difference in certificate production date of 5 seconds (validFrom) 
            - have the same telematikId
        """
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        List<Cert> certs = [
                new Cert(type: CertType.RSA, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
                new Cert(type: CertType.ECC, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
        ]

        when:
        List<Cert> result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "No RSA certificates to be filtered out"
        result == [
                certs[1]
        ]

        where:
        classUnderTest << classesUnderTest
    }

    @Unroll("Class: #classUnderTest | Certs: #certsList")
    def "1 - ECC Certificates must not be filtered at all"() {
        given:
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        List<Cert> certs = certsList

        when:
        List<Cert> result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "No ECC certificates to be filtered out"
        result.toSet() == [
                certs[0],
                certs[1]
        ].toSet()

        where:
        [[description, certsList], classUnderTest] << [[
       [
               """
                A clientmodul with a list of 2 certificates which:
                - hold no ICCSNs
                - are from identical certificate type (ECC)
                - have a time difference in certificate production date of 5 seconds (validFrom)
                - have the same telematikId
                """,
               [
                       new Cert(type: CertType.ECC, iccsn: null, validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
                       new Cert(type: CertType.ECC, iccsn: null, validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
               ]
               ],
               [
               """
               A clientmodul with a list of 2 certificates which:
                - hold identical ICCSNs, indicating coming from the same SMC-B
                - are from identical certificate type (ECC)
                - have a time difference in certificate production date of 5 seconds (validFrom)
                - have the same telematikId
               """,
                   [
                       new Cert(type: CertType.ECC, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
                       new Cert(type: CertType.ECC, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
                   ]
               ]
       ], classesUnderTest].combinations()
    }

    def "2 - If an RSA certificate has the same ICCSN as another RSA Certificate, it shall not be filtered at all (neither for ICCSN nor for time proximity."() {
        given: """
        A clientmodul with a list of 2 certificates which:
        - hold identical ICCSNs, indicating coming from the same SMC-B
        - are from identical certificate type (RSA)
        - have a time difference in certificate production date of 5 seconds (validFrom)
        - have the same telematikId
        """
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        List<Cert> certs = [
                new Cert(type: CertType.RSA, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
                new Cert(type: CertType.RSA, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
        ]

        when:
        List<Cert> result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "No RSA certificates shall be filtered out"
        result.toSet() == [
                certs[0],
                certs[1]
        ].toSet()

        where:
        classUnderTest << classesUnderTest
    }
    def "3 - If an RSA certificate within time proximity to an ECC Certificate has no ICCSN, it shall be filtered for time proximity."() {
        given: """
    A clientmodul with a list of 2 certificates which:
    - hold no ICCSNs
    - differ in their certificate type of ECC and RSA
    - have a time difference in certificate production date of -5 seconds (validFrom)
    - have the same telematikId
    """
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        List<Cert> certs = [
            new Cert(type: CertType.ECC, iccsn: null, validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
            new Cert(type: CertType.RSA, iccsn: null, validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
        ]

        when:
        List<Cert> result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "The RSA certificate shall be filtered out"
        result == [
            certs[0]
        ]

        where:
        classUnderTest << classesUnderTest
    }

    def "4 - RSA certificates shall not be filtered due to another RSA Certificate - neither for ICCSN nor for time proximity"() {
        given: """
    A clientmodul with a list of 2 certificates which:
    - hold no ICCSNs
    - are from identical certificate type (RSA)
    - have a time difference in certificate production date of 5 seconds (validFrom)
    - have the same telematikId
    """
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        List<Cert> certs = [
            new Cert(type: CertType.RSA, iccsn: null, validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
            new Cert(type: CertType.RSA, iccsn: null, validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
        ]

        when:
        List<Cert> result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "No RSA certificates shall be filtered out"
        result.toSet() == [
                certs[0],
                certs[1]
        ].toSet()

        where:
        classUnderTest << classesUnderTest
    }

    @Unroll("Class: #classUnderTest | Certs: #certsList")
    def "6 - If an RSA certificate within time proximity to an ECC Certificate has an ICCSN, it shall not be filtered for time proximity."() {
        given:
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        List<Cert> certs = certsList

        when:
        List<Cert> result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "The RSA certificate shall not be filtered out"
        result.toSet() == [
                certs[0],
                certs[1]
        ].toSet()

        where:
        [[description, certsList], classUnderTest] << [[
            [
                """
                A clientmodul with a list of 2 certificates which:
                - hold different ICCSNs, indicating coming from different SMC-Bs
                - differ in their certificate type of ECC and RSA
                - have a time difference in certificate production date of 5 seconds (validFrom)
                - have the same telematikId
                """,
                [
                        new Cert(type: CertType.ECC, iccsn: "abc", validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
                        new Cert(type: CertType.RSA, iccsn: "def", validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
                ]
            ],
            [
                """
                A clientmodul with a list of 2 certificates which:
                - only one holds an ICCSN
                - differ in their certificate type of ECC and RSA
                - have a time difference in certificate production date of 5 seconds (validFrom)
                - have the same telematikId
                """,
                [
                        new Cert(type: CertType.ECC, iccsn: null, validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
                        new Cert(type: CertType.RSA, iccsn: "def", validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
                ]
            ]
        ], classesUnderTest].combinations()
    }
}

//TODO: If an RSA certificate outside time proximity to an ECC Certificate has no ICCSN, it shall not be filtered for time proximity.

//TODO: Test cases with more than 2 certificates

//TODO: Test cases with more than 2 certificates and different telematikIds

//TODO: Test cases with more than 2 certificates and different certificate types

//TODO: Test cases with more than 2 certificates and different certificate types and different telematikIds

//TODO: Test cases with more than 2 certificates and different certificate types and different telematikIds and different ICCSNs

//TODO: Test cases with more than 2 certificates and different certificate types and different telematikIds and different ICCSNs and different time proximity
