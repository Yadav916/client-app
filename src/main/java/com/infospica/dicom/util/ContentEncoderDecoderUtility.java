package com.infospica.dicom.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;

public class ContentEncoderDecoderUtility {

    private ContentEncoderDecoderUtility() {
        super();
    }

    public static String getEncryptedContent(String encodedInput) throws Exception {
        byte[] byteInitVector = Security.generateNewInitVector();
        byte[] byteGenKey = Security.generateNewSecretKey();

        int chrLength = 0;
        String generatedKey = Base64.getEncoder().withoutPadding().encodeToString(byteGenKey);
        String initVector = Base64.getEncoder().withoutPadding().encodeToString(byteInitVector);
        generatedKey = RandomStringUtils.randomAlphabetic(1) + (generatedKey.length()) + generatedKey;
        initVector = initVector + ((chrLength = initVector.length()) < 10 ? "0" + chrLength : chrLength)
                + RandomStringUtils.randomAlphabetic(1);

        String encryptedContent = Security.encrypt(encodedInput, byteInitVector, byteGenKey);
        encryptedContent = generatedKey + encryptedContent + initVector;
        return encryptedContent;
    }

    public static String getDecryptedContent(String encryptedInput) throws Exception {
        int encryptedStrLength = encryptedInput.length();
        //split init vector
        Integer initVectorLength = Integer.valueOf(StringUtils.substring(
                StringUtils.substring(encryptedInput, encryptedStrLength - 3), 0, 2));
        Integer initVectorStartLength = encryptedStrLength - initVectorLength - 3;
        String initVectorString = StringUtils.substring(encryptedInput, initVectorStartLength, encryptedStrLength - 3);

        //split secret key
        Integer generatedKeyLength = Integer.valueOf(StringUtils.substring(
                StringUtils.substring(encryptedInput, 0, 3), 1, 3));
        String generatedKeyString = StringUtils.substring(encryptedInput, 3, (generatedKeyLength + 3));

        String encryptedString = StringUtils.substring(encryptedInput, (generatedKeyLength + 3), initVectorStartLength);

        byte[] byteInitVector = Base64.getDecoder().decode(initVectorString);
        byte[] byteGenKey = Base64.getDecoder().decode(generatedKeyString);

        String decryptedString = Security.decrypt(encryptedString, byteInitVector, byteGenKey);
        return new String(Base64.getDecoder().decode(decryptedString));
    }
}
