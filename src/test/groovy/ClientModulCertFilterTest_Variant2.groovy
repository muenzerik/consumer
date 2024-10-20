import spock.lang.Specification

import java.text.SimpleDateFormat

class ClientModulCertFilterTest_Variant2 extends Specification {
    
    def """
    A clientmodul shall filter out the RSA Certificates from a recipient's extracted VZD-list,
    if an ECC certificate from the same SMC-B is in the list as well
    """() {
        given: """
        A clientmodul with a list of 2 certificates which:
        - hold identical ICCSNs, indicating coming from the same SMC-B
        - differ in their certificate type of ECC and RSA
        - have a time difference in certificate production date of 5 seconds (validFrom)
        - have the same telematikId
        """
        def clientModul = new ClientModul()
        def dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        def certs = [
            new Cert(type: CertType.RSA, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:00"), telematikId: "123"),
            new Cert(type: CertType.ECC, iccsn: "1234567890", validFrom: dateFormat.parse("2023-10-01 12:00:05"), telematikId: "123")
        ]

        when:
        def result = clientModul.weedOutweedOutUnnecessaryRsaCerts(certs)

        then: "The RSA certificate shall be filtered out"
        result == [
            certs[1]
        ]
    }
}