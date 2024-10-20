

class ClientModul {

    static final int TIME_WINDOW = 75

    /**
     * Filters out unnecessary RSA certificates from the provided list of certificates.
     *
     * @param certs the list of certificates to be filtered
     * @return a list of certificates with unnecessary RSA certificates removed
     */
    static List<Cert> weedOutUnnecessaryRsaCerts(List<Cert> certs ){
        def certsRsa = certs.findAll{it.type == CertType.RSA}
        def certsEcc = certs.findAll{it.type == CertType.ECC}

        def filteredCerts = certsRsa.findAll { c ->
            certsEcc.any { it.iccsn == c.iccsn && c.iccsn != null } ||
            certsEcc.findAll { Math.abs(it.validFrom - c.validFrom) / 1000 <= TIME_WINDOW }
                .findAll { it.telematikId == c.telematikId }
                .size() == 1
        }
        filteredCerts += certsEcc
        return filteredCerts
    }



    List<Cert> weedOutweedOutUnnecessaryRsaCerts(List<Cert> inputCerts ){
        def certsPerTelematikId = inputCerts.groupBy { c -> c.telematikId }
        def result = []
        for(inputCertsPerTelematikId in certsPerTelematikId.values()) {
            result.addAll( weedOutUnnecessaryRsaCertsForSameTelematikId(inputCertsPerTelematikId) )
        }
        return result
    }

    List<Cert> weedOutUnnecessaryRsaCertsForSameTelematikId(List<Cert> inputCerts){
        def resultList = []
        for(cert in inputCerts) {
            if(cert.type == CertType.RSA) {
                if ( hasIccsn(cert) ) {
                    def maybeEccCert = findEccWithMatchingIccsn(cert, inputCerts)
                    handleMaybeEccCert(maybeEccCert, cert, resultList, inputCerts)
                } else {
                    def maybeEccCert = findEccCertsWithinTimeProximity(cert, inputCerts)
                    handleMaybeEccCert(maybeEccCert, cert, resultList, inputCerts)
                }
            }
        }
        if (inputCerts.find { c -> c.type == CertType.ECC }.isNotEmpty()) {
            throw IllegalStateException()
        }
        resultList.addAll(inputCerts)
        return resultList
    }

    static boolean hasIccsn(Cert cert) {
        return cert.iccsn != null
    }

    /**
     * Assembles a list of certificates from a given list of certificates. if it matches with a given certificate.
     *
     * @param cert the certificate from which its ICCSN has to be taken for match comparison
     * @param inputCerts the list of certificates from which to extract the certs with same ICCSN than in cert
     * @return the ICCSN extracted from the certificate or null if the certificate does not contain an ICCSN
     */
    static Cert findEccWithMatchingIccsn(Cert cert, List<Cert> inputCerts){
        return inputCerts.find { c -> cert.iccsn == c.iccsn && cert != c && c.type == CertType.ECC &&
                cert.iccsn != null && c.iccsn != null }
                .find()
    }

    /**
     * Extracts the ICCSN from the given certificate.
     *
     * @param cert the certificate from which to extract the ICCSN
     * @return the ICCSN extracted from the certificate or null if the certificate does not contain an ICCSN
     */
    Cert findEccCertsWithinTimeProximity(Cert cert, List<Cert> inputCerts){
        if (extractIccsn(cert) != null) {
            throw IllegalStateException()
        }
        inputCerts.find { c -> abs(c.validFrom - cert.validFrom) / 1000 <= TIME_WINDOW && cert != c && c.type == CertType.ECC &&
                c.iccsn == null }
                .sortAsc { c -> abs(c.validFrom - cert.validFrom) }
                .find()
    }

    static void handleMaybeEccCert(Cert maybeEccCert, Cert rsaCert, List<Cert> resultList, List<Cert> inputList){
        if(maybeEccCert != null) {
            resultList.add(maybeEccCert)
            //TODO: below removals mess up the iterator in line 40
            inputList.remove(rsaCert)
            inputList.remove(maybeEccCert)
        } else {
            resultList.add(rsaCert) // an dieses RSA cert müssen wir verschlüsseln
        }
    }
}
