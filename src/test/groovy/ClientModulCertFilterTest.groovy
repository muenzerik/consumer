import spock.lang.Specification
import spock.lang.Unroll
import spock.lang.Shared
import java.text.SimpleDateFormat

@Unroll
class ClientModulCertFilterTest extends Specification {

    @Shared
    def classesUnderTest = ["ClientModul_Variant1", "ClientModul_Variant2"]

    @Shared
    def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    def "0"() {
        given: """
            A clientmodul with a list of 2 certificates which: 
            - hold identical ICCSNs, indicating coming from the same SMC-B \n
            - differ in their certificate type of ECC and RSA 
            - have a time difference in certificate production date of 5 seconds (validFrom) 
            - have the same telematikId
        """
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        def certs = [
                new Cert(type: CertType.RSA, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
                new Cert(type: CertType.ECC, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
        ]

        when:
        def result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "No RSA certificates to be filtered out"
        result == [
                certs[1]
        ]

        where:
        classUnderTest << classesUnderTest
    }

    def "1"() {
        given: """
        A clientmodul with a list of 2 certificates which:
        - hold identical ICCSNs, indicating coming from the same SMC-B
        - are from identical certificate type (ECC)
        - have a time difference in certificate production date of 5 seconds (validFrom)
        - have the same telematikId
        """
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        def certs = [
            new Cert(type: CertType.ECC, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
            new Cert(type: CertType.ECC, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
        ]

        when:
        def result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "No RSA certificates to be filtered out"
        result == [
            certs[0],
            certs[1]
        ]

        where:
        classUnderTest << classesUnderTest
    }
    def "2"() {
        given: """
        A clientmodul with a list of 2 certificates which:
        - hold identical ICCSNs, indicating coming from the same SMC-B
        - are from identical certificate type (RSA)
        - have a time difference in certificate production date of 5 seconds (validFrom)
        - have the same telematikId
        """
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        def certs = [
                new Cert(type: CertType.RSA, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
                new Cert(type: CertType.RSA, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
        ]

        when:
        def result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "No RSA certificates shall be filtered out"
        result == [
            certs[0],
            certs[1]
        ]

        where:
        classUnderTest << classesUnderTest
    }
    def "3"() {
        given: """
    A clientmodul with a list of 2 certificates which:
    - hold no ICCSNs
    - differ in their certificate type of ECC and RSA
    - have a time difference in certificate production date of -5 seconds (validFrom)
    - have the same telematikId
    """
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        def certs = [
            new Cert(type: CertType.ECC, iccsn: null, validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
            new Cert(type: CertType.RSA, iccsn: null, validFrom: dateFormat.parse("2023-10-01 11:59:55"), telematikId: "123")
        ]

        when:
        def result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "The RSA certificate shall be filtered out"
        result == [
            certs[0]
        ]

        where:
        classUnderTest << classesUnderTest
    }
    def "4"() {
        given: """
        A clientmodul with a list of 2 certificates which:
        - hold no ICCSNs
        - are from identical certificate type (ECC)
        - have a time difference in certificate production date of 5 seconds (validFrom)
        - have the same telematikId
        """
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        def certs = [
                new Cert(type: CertType.ECC, iccsn: null, validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
                new Cert(type: CertType.ECC, iccsn: null, validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
        ]

        when:
        def result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "No RSA certificates to be filtered out"
        result == [
            certs[0],
            certs[1]
        ]

        where:
        classUnderTest << classesUnderTest
    }
    def "5"() {
        given: """
    A clientmodul with a list of 2 certificates which:
    - hold no ICCSNs
    - are from identical certificate type (RSA)
    - have a time difference in certificate production date of 5 seconds (validFrom)
    - have the same telematikId
    """
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        def certs = [
            new Cert(type: CertType.RSA, iccsn: null, validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
            new Cert(type: CertType.RSA, iccsn: null, validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
        ]

        when:
        def result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "No RSA certificates shall be filtered out"
        result == [
            certs[0],
            certs[1]
        ]

        where:
        classUnderTest << classesUnderTest
    }
    def "6"() {
        given: """
        A clientmodul with a list of 2 certificates which:
        - hold different ICCSNs, indicating coming from different SMC-Bs
        - differ in their certificate type of ECC and RSA
        - have a time difference in certificate production date of 5 seconds (validFrom)
        - have the same telematikId
        """
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        def certs = [
                new Cert(type: CertType.ECC, iccsn: "abc", validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
                new Cert(type: CertType.RSA, iccsn: "def", validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
        ]

        when:
        def result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "The RSA certificate shall not be filtered out"
        result == [
                certs[0],
                certs[1]
        ]

        where:
        classUnderTest << classesUnderTest
    }
    def "7"() {
        given: """
    A clientmodul with a list of 2 certificates which:
    - only one holds an ICCSN
    - differ in their certificate type of ECC and RSA
    - have a time difference in certificate production date of 5 seconds (validFrom)
    - have the same telematikId
    """
        def clientModul = new GroovyShell().evaluate("new ${classUnderTest}()")
        def certs = [
            new Cert(type: CertType.ECC, iccsn: null, validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
            new Cert(type: CertType.RSA, iccsn: "def", validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
        ]

        when:
        def result = clientModul.weedOutUnnecessaryRsaCerts(certs)

        then: "The RSA certificate shall not be filtered out"
        result == [
            certs[0],
            certs[1]
        ]

        where:
        classUnderTest << classesUnderTest
    }
}
