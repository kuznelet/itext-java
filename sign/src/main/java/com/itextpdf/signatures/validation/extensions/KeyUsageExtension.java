/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
    Authors: Apryse Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.signatures.validation.extensions;

import com.itextpdf.bouncycastleconnector.BouncyCastleFactoryCreator;
import com.itextpdf.commons.bouncycastle.IBouncyCastleFactory;
import com.itextpdf.signatures.OID;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

/**
 * Class representing "Key Usage" extenstion.
 */
public class KeyUsageExtension extends CertificateExtension {

    private static final IBouncyCastleFactory FACTORY = BouncyCastleFactoryCreator.getFactory();

    private final int keyUsage;

    /**
     * Create new {@link KeyUsageExtension} instance using provided {@code int} flag.
     *
     * @param keyUsage {@code int} flag which represents bit values for key usage value
     */
    public KeyUsageExtension(int keyUsage) {
        super(OID.X509Extensions.KEY_USAGE, FACTORY.createKeyUsage(keyUsage).toASN1Primitive());
        this.keyUsage = keyUsage;
    }

    /**
     * Create new {@link KeyUsageExtension} instance using provided key usage enum list.
     *
     * @param keyUsages key usages {@link List} which represents key usage values
     */
    public KeyUsageExtension(List<KeyUsage> keyUsages) {
        this(convertKeyUsageSetToInt(keyUsages));
    }

    /**
     * Create new {@link KeyUsageExtension} instance using provided single key usage enum value.
     *
     * @param keyUsageValue {@link KeyUsage} which represents single key usage enum value
     */
    public KeyUsageExtension(KeyUsage keyUsageValue) {
        this(Collections.singletonList(keyUsageValue));
    }

    /**
     * Check if this extension is present in the provided certificate. In case of {@link KeyUsageExtension},
     * check if this key usage bit values are present in certificate. Other values may be present as well.
     *
     * @param certificate {@link X509Certificate} in which this extension shall be present
     *
     * @return {@code true} if this key usage bit values are present in certificate, {@code false} otherwise
     */
    @Override
    public boolean existsInCertificate(X509Certificate certificate) {
        boolean[] providedKeyUsageFlags = certificate.getKeyUsage();
        if (providedKeyUsageFlags == null) {
            return false;
        }
        for (int i = 0; i < providedKeyUsageFlags.length; ++i) {
            int power = providedKeyUsageFlags.length - i - 2;
            if (power < 0) {
                // Bits are encoded backwards, for the last bit power is -1 and in this case we need to go over byte
                power = 16 + power;
            }
            if ((keyUsage & (1 << power)) != 0 && !providedKeyUsageFlags[i]) {
                return false;
            }
        }
        return true;
    }

    private static int convertKeyUsageSetToInt(List<KeyUsage> keyUsages) {
        KeyUsage[] possibleKeyUsage = new KeyUsage[] {
                KeyUsage.DIGITAL_SIGNATURE,
                KeyUsage.NON_REPUDIATION,
                KeyUsage.KEY_ENCIPHERMENT,
                KeyUsage.DATA_ENCIPHERMENT,
                KeyUsage.KEY_AGREEMENT,
                KeyUsage.KEY_CERT_SIGN,
                KeyUsage.CRL_SIGN,
                KeyUsage.ENCIPHER_ONLY,
                KeyUsage.DECIPHER_ONLY
        };
        int result = 0;
        for (int i = 0; i < possibleKeyUsage.length; ++i) {
            if (keyUsages.contains(possibleKeyUsage[i])) {
                int power = possibleKeyUsage.length - i - 2;
                if (power < 0) {
                    // Bits are encoded backwards, for the last bit power is -1 and in this case we need to go over byte
                    power = 16 + power;
                }
                result |= (1 << power);
            }
        }
        return result;
    }
}
