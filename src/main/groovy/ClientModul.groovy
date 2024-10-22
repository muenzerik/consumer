class ClientModul_Variant1 {

    static final int TIME_WINDOW = 75

    /**
     * Filters out unnecessary RSA certificates from the provided list of certificates.
     * A certificate is considered unnecessary if an ECC certificate from the same SMC-B is in the list as well.
     *
     * @param certs the list of certificates to be filtered
     * @return a list of certificates with unnecessary RSA certificates removed
     */
    static List<Cert> weedOutUnnecessaryRsaCerts(List<Cert> certs) {
        def certsRsa = certs.findAll { it.type == CertType.RSA }
        def certsEcc = certs.findAll { it.type == CertType.ECC }

        def filteredCerts = certsRsa.findAll { c ->
            certsEcc.any { it.iccsn == c.iccsn && c.iccsn != null } ||
                    certsEcc.findAll { Math.abs(it.validFrom.getTime() - c.validFrom.getTime()) / 1000 <= TIME_WINDOW }
                            .findAll { it.telematikId == c.telematikId }
                            .size() == 1
        }
        return certs - filteredCerts
    }
}

class ClientModul_Variant2 {

    static final int TIME_WINDOW = 75

    //TODO: Consider whether the linkage and flag to a partnering cert is really necessary
    class RecipientCert extends Cert {
        Boolean hasPartneringCertOnSameSmcB = false
        Cert partnerCert = null
        Cert originalCert

        RecipientCert(Cert cert) {
            super(cert.iccsn, cert.telematikId, cert.validFrom, cert.type)
            this.originalCert = cert
        }
    }

    /**
     * Filters out unnecessary RSA certificates from the provided list of certificates.
     * A certificate is considered unnecessary if an ECC certificate from the same SMC-B is in the list as well.
     *
     * @param certs the list of certificates to be filtered
     * @return a list of certificates with unnecessary RSA certificates removed
     */
    List<Cert> weedOutUnnecessaryRsaCerts(List<Cert> inputCerts ){
        def certsPerTelematikId = inputCerts.groupBy { c -> c.telematikId }
        def result = []
        for(inputCertsPerTelematikId in certsPerTelematikId.values()) {
            result.addAll( weedOutUnnecessaryRsaCertsForSameTelematikId(inputCertsPerTelematikId) )
        }
        return result
    }

    List<Cert> weedOutUnnecessaryRsaCertsForSameTelematikId(List<Cert> inputCerts){
        def recipientCerts = inputCerts.collect {new RecipientCert(it) }

        def redundantRsaCerts = []
        for(cert in recipientCerts.findAll({it.type == CertType.ECC})) {
             if ( cert.iccsn != null ) {
                redundantRsaCerts?.add(findRsaWithMatchingIccsn(cert, recipientCerts))
            } else {
                 redundantRsaCerts?.add(findEccCertsWithinTimeProximity(cert, recipientCerts))
            }
        }
        return (recipientCerts - redundantRsaCerts).collect({it.originalCert})
    }

    static RecipientCert findRsaWithMatchingIccsn(RecipientCert cert, List<RecipientCert> inputCerts){
        def matchingCert = inputCerts.find { c ->
            cert.iccsn == c.iccsn &&
            cert != c &&
            c.type == CertType.RSA &&
            cert.iccsn != null &&
            c.iccsn != null
        }

        if (matchingCert != null) {
            cert.hasPartneringCertOnSameSmcB = true
            cert.partnerCert = matchingCert
            matchingCert.hasPartneringCertOnSameSmcB = true
            matchingCert.partnerCert = cert
        }

        return matchingCert
    }

    static RecipientCert findEccCertsWithinTimeProximity(RecipientCert cert, List<RecipientCert> inputCerts){
        if (cert.iccsn != null) {
            throw new IllegalStateException()
        }
        def matchingCert = inputCerts.find { c ->
            Math.abs(c.validFrom.getTime() - cert.validFrom.getTime()) / 1000 <= TIME_WINDOW &&
                    cert != c && c.type == CertType.ECC &&
                    c.iccsn == null
        }

        if (matchingCert != null) {
            cert.hasPartneringCertOnSameSmcB = true
            cert.partnerCert = matchingCert
            matchingCert.hasPartneringCertOnSameSmcB = true
            matchingCert.partnerCert = cert
        }

        return matchingCert
    }
}
