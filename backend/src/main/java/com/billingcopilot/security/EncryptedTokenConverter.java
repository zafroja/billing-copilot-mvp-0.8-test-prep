package com.billingcopilot.security;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
/** Encrypt before persistence and decrypt on reading the JPA column. */
@Converter
public class EncryptedTokenConverter implements AttributeConverter<String,String> {
 @Override public String convertToDatabaseColumn(String value){return TokenCrypto.encrypt(value);}
 @Override public String convertToEntityAttribute(String value){return TokenCrypto.decrypt(value);}
}
