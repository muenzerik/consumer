class Cert {
    String iccsn
    String telematikId
    Date validFrom
    CertType type
}

enum CertType {
    RSA,
    ECC
}