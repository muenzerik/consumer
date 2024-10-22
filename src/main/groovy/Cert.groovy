class Cert {
    String iccsn
    String telematikId
    Date validFrom
    CertType type

    Cert(String iccsn, String telematikId, Date validFrom, CertType type) {
        this.iccsn = iccsn
        this.telematikId = telematikId
        this.validFrom = validFrom
        this.type = type
    }

    Cert(LinkedHashMap<String, Object> map) {
        this.iccsn = map['iccsn']
        this.telematikId = map['telematikId']
        this.validFrom = map['validFrom']
        this.type = map['type']
    }
}

enum CertType {
    RSA,
    ECC
}