package com.ncr.zatca.greencore.data.b2b;

import lombok.Data;
import java.util.List;

@Data
public class Party {
    private List<PartyIdentification> partyIdentification;
    private PostalAddress postalAddress;
    private PartyTaxScheme partyTaxScheme;
    private PartyLegalEntity partyLegalEntity;
}
